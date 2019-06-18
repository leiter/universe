package com.together.addpicture

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.IOException

interface AddPicture {
    fun startAddPhoto(): Disposable
}

class AddPictureImpl(private val fragment: Fragment) : AddPicture {

    private lateinit var dialogFragment: ChooseDialog

    private lateinit var actions: PublishSubject<ChooseDialog.Action>


    override fun startAddPhoto(): Disposable {

        val ft = fragment.fragmentManager!!.beginTransaction()
        val prev: Fragment? = fragment.fragmentManager!!.findFragmentByTag(ChooseDialog.TAG)
        if (prev != null) {
            ft.remove(prev).commit()
        }
        dialogFragment = ChooseDialog()
        dialogFragment.show(fragment.fragmentManager, ChooseDialog.TAG)

        actions = dialogFragment.actionChannel

        return actions.subscribe {
            when (it) {
                ChooseDialog.Action.TAKE_PICKTURE -> startTakePicture()
                ChooseDialog.Action.CHOOSE_PICTURE -> startPickImage()
                ChooseDialog.Action.DELETE_PHOTO() -> deleteImage()
                ChooseDialog.Action.CANCEL_ADD_PICTURE -> {

                }
                is ChooseDialog.Action.PermissionResult -> onPermissionResult(it)
                is ChooseDialog.Action.ActivityResult -> onActivityResult(it)
            }
        }

    }

    private fun deleteImage() {


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
        val granted = ContextCompat.checkSelfPermission(fragment.context!!, Manifest.permission.CAMERA)
        when (granted) {
            PackageManager.PERMISSION_GRANTED -> startCamera()
            else -> dialogFragment.requestPermissions(arrayOf(Manifest.permission.CAMERA),
                REQUEST_PIC_PICTURE_PERMISSION)
        }
    }

    private fun startCamera() {

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePicture ->
            takePicture.resolveActivity(fragment.context!!.packageManager)
                ?.also {
                    val file: File? = try {
                        createFile(fragment.requireContext())
                    } catch (ex: IOException) {
                        return
                    }
                    file?.also {
                        val fileUri: Uri = FileProvider.getUriForFile(
                            fragment.context!!, fragment.context!!.packageName, it
                        )
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                        dialogFragment.startActivityForResult(takePicture, REQUEST_TAKE_PICTURE)
                    }
                }
        }

    }

    private fun onPermissionResult(perm: ChooseDialog.Action.PermissionResult) {
        if (perm.permissions.size == 1 &&
            perm.requestCode == REQUEST_PIC_PICTURE_PERMISSION &&
            perm.permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
            perm.grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startPickIntent()
        }

        if (perm.permissions.size == 1 &&
            perm.requestCode == REQUEST_TAKE_PICTURE_PERMISSION &&
            perm.permissions[0] == Manifest.permission.CAMERA &&
            perm.grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    private fun onActivityResult(result: ChooseDialog.Action.ActivityResult) {

        if (result.resultCode == Activity.RESULT_OK) {


        }


    }

    private fun createFile(context: Context, tmpName: String = "toBeOverridden.jpg")
            : File = File(context.filesDir, tmpName)


    private fun startPickIntent() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            .apply {
                setType(IMAGE_TYPE)
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        dialogFragment.startActivityForResult(getIntent, REQUEST_PIC_PICTURE)
    }

    private fun startPickImage() {
        val granted =
            ContextCompat.checkSelfPermission(fragment.context!!, android.Manifest.permission.READ_EXTERNAL_STORAGE)
        when (granted) {
            PackageManager.PERMISSION_GRANTED -> startPickIntent()
            else -> {
                dialogFragment.requestPermissions(
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


