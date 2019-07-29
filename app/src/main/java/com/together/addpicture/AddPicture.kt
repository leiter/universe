package com.together.addpicture

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.together.create.app.MainMessagePipe
import com.together.repository.Result
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.IOException
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
                ChooseDialog.Action.DELETE_PHOTO() -> {          }
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
        val granted = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        when (granted) {
            PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> dialogFragment.requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PIC_PICTURE_PERMISSION
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

        if (permissions.size == 1 &&
            requestCode == REQUEST_TAKE_PICTURE_PERMISSION &&
            permissions[0] == Manifest.permission.CAMERA &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {         startCamera()       }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {

            val fileUri: Uri

            if (requestCode == REQUEST_TAKE_PICTURE) {
                fileUri = FileProvider.getUriForFile(activity, activity.packageName, currentImageFile)
                val image = Result.NewImageCreated(fileUri.path ?: "noFound", fileUri)
                MainMessagePipe.mainThreadMessage.onNext(image)
            } else if (requestCode == REQUEST_PIC_PICTURE) {
                if (data != null) {
                    val imageColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val id = data.data?.lastPathSegment!!.split(":")[1]
                    val cursor = activity.contentResolver.query(
                        getUri(),
                        imageColumn,
                        MediaStore.Images.Media._ID + "=" + id, null, null
                    )
                    cursor!!.moveToFirst()
                    val uriString = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                    fileUri = Uri.parse(uriString)
                    cursor.close()
                    val image = Result.NewImageCreated(fileUri.path ?: "noFound", fileUri)
                    MainMessagePipe.mainThreadMessage.onNext(image)
                }
            }
        }
        activity.finish()
    }

    private fun createFile(context: Context, tmpName: String = someString()): File {
        currentImageFile = File(context.filesDir, tmpName)
        return currentImageFile
    }

    private fun someString() = UUID.randomUUID().toString() + ".jpg"

    private fun startPickIntent() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            .apply {
                type = IMAGE_TYPE
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        activity.startActivityForResult(getIntent, REQUEST_PIC_PICTURE)
    }

    private fun startPickImage() {
        val granted =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        when (granted) {
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


