package com.together.addpicture

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.together.base.MainMessagePipe
import com.together.repository.Result
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

interface AddPicture {
    fun startAddPhoto(): Disposable
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}

class AddPictureImpl(private val activity: AppCompatActivity) : AddPicture {

    private lateinit var dialogFragment: ChooseDialog

    private lateinit var actions: PublishSubject<ChooseDialog.Action>

    private lateinit var currentImageFile: File

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun startAddPhoto(): Disposable {

        val ft = activity.supportFragmentManager.beginTransaction()
        val prev: Fragment? = activity.supportFragmentManager.findFragmentByTag(ChooseDialog.TAG)
        if (prev != null) {
            ft.remove(prev).commit()
        }
        dialogFragment = ChooseDialog()
        dialogFragment.show(activity.supportFragmentManager, ChooseDialog.TAG)

        actions = dialogFragment.actionChannel

        return actions.subscribe {
            when (it) {
                ChooseDialog.Action.TAKE_PICKTURE -> startTakePicture()
                ChooseDialog.Action.CHOOSE_PICTURE -> startPickImage()
                ChooseDialog.Action.DELETE_PHOTO -> {          }
                ChooseDialog.Action.CANCEL_ADD_PICTURE -> activity.finish()
            }
        }
    }

    companion object {

        const val TAG = "AddPicture"
        const val IMAGE_TYPE = "image/*"

        const val REQUEST_TAKE_PICTURE = 22
        const val REQUEST_TAKE_PICTURE_PERMISSION = 22
        const val REQUEST_PIC_PICTURE = 222
        const val REQUEST_PIC_PICTURE_PERMISSION = 222

    }


    private fun startTakePicture() {
        when (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> ActivityCompat.requestPermissions(activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_TAKE_PICTURE_PERMISSION
            )
        }
    }

    private fun startCamera() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePicture ->
            takePicture.resolveActivity(activity.packageManager)
                ?.also {
                    val file: File? = try {
                        createFile(activity)
                    } catch (ex: IOException) {
                        return
                    }
                    file?.also {
                        val fileUri: Uri = FileProvider.getUriForFile(
                            activity, activity.packageName, it
                        )
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                        activity.startActivityForResult(takePicture, REQUEST_TAKE_PICTURE)
                    }
                }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (permissions.size == 1 &&
            requestCode == REQUEST_PIC_PICTURE_PERMISSION &&
            permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {         startPickIntent()       }
        else activity.finish()

        if (permissions.size == 1 &&
            (requestCode == REQUEST_TAKE_PICTURE_PERMISSION || requestCode==65758) &&  //todo
            permissions[0] == Manifest.permission.CAMERA &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {         startCamera()       }
        else activity.finish()
    }
    fun getPathFromURI(contentUri: Uri ): String {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor : Cursor? = activity.contentResolver.query(
            contentUri, proj, "", null, "")
        if (cursor?.moveToFirst()!!) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        return res!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val fileUri: Uri
            if (requestCode == REQUEST_TAKE_PICTURE) {
                fileUri = FileProvider.getUriForFile(activity, activity.packageName, currentImageFile)
                compositeDisposable.add(Single.just(fileUri).subscribeOn(Schedulers.io()).map {
                    val orgBitmap = MediaStore.Images.Media.getBitmap(activity.contentResolver, fileUri)
                    rotateImageIfNeeded(orgBitmap, fileUri)?.compress(
                        Bitmap.CompressFormat.PNG, 100, FileOutputStream(currentImageFile))
                    orgBitmap.recycle()

                }.observeOn(AndroidSchedulers.mainThread()).subscribe({
                    val image = Result.NewImageCreated(fileUri)
                    MainMessagePipe.mainThreadMessage.onNext(image)
                    activity.finish()
                }, {

                }))

            } else if (requestCode == REQUEST_PIC_PICTURE) {
                if (data != null) {
//                    val imageColumn = arrayOf(MediaStore.Images.Media.DATA)
//                    val id = data.data?.lastPathSegment!!
//                    val cursor = activity.contentResolver.query(
//                            getUri(),
//                        imageColumn,
//                        MediaStore.Images.Media._ID + "=" + id, null, null
//                    )
//                    cursor!!.moveToFirst()
//                    val uriString = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
//                    cursor.close()
//                    fileUri = Uri.fromFile(File(getPathFromURI(data.data!!)))
//                    val image = Result.NewImageCreated(fileUri)
                    val image = Result.NewImageCreated(data.data)
                    MainMessagePipe.mainThreadMessage.onNext(image)
                    activity.finish()
                }
            }
        }
    }



    private fun getRotation(inputStream: InputStream): Int {
        val exif = ExifInterface(inputStream)
        val result = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
        val rotation: Int = when (result) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        return rotation
    }


    private fun rotateImageIfNeeded(img: Bitmap, selectedImg: Uri) : Bitmap? {
        val inputStream = activity.contentResolver.openInputStream(selectedImg)
        val ei : ExifInterface = if (Build.VERSION.SDK_INT > 23 && inputStream != null) {
            ExifInterface(inputStream)
        } else {
            ExifInterface(selectedImg.path!!)
        }
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)){
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img,180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img,270F)
            else -> null
        }
    }

    private fun rotateImage(bitMap: Bitmap, degree: Float) : Bitmap {
        val m = Matrix(); m.postRotate(degree)
        return Bitmap.createBitmap(bitMap,0,0,
            bitMap.width, bitMap.height,m, true)
    }

    private fun createFile(context: Context, tmpName: String = someString()): File {
        currentImageFile = File(context.filesDir, tmpName)
        return currentImageFile
    }

    private fun someString() = "${UUID.randomUUID()}.jpg"

    private fun startPickIntent() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            .apply {
                type = IMAGE_TYPE
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        activity.startActivityForResult(getIntent, REQUEST_PIC_PICTURE)
    }

    private fun startPickImage() {
        when (ContextCompat.checkSelfPermission(activity,
            Manifest.permission.READ_EXTERNAL_STORAGE)) {
            PackageManager.PERMISSION_GRANTED -> startPickIntent()
            else -> {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PIC_PICTURE_PERMISSION
                )
            }
        }
    }

    private fun getUri(): Uri {
        val state: String = Environment.getExternalStorageState()
        if (state.toLowerCase() != Environment.MEDIA_MOUNTED.toLowerCase()) {
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI
        }
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}


