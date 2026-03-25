package com.example.cabshare.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cabshare.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                _isLoading.value = false
                if (document != null && document.exists()) {
                    _user.value = document.toObject(User::class.java)
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _statusMessage.value = "Failed to load profile: ${e.message}"
            }
    }

    fun saveProfile(contentResolver: ContentResolver, name: String, bio: String, selectedImageUri: Uri?) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true

        if (selectedImageUri != null) {
            try {
                // 1. Get orientation from Exif
                val orientation = getOrientation(contentResolver, selectedImageUri)

                // 2. Decode bitmap
                val inputStream: InputStream? = contentResolver.openInputStream(selectedImageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) {
                    _isLoading.value = false
                    _statusMessage.value = "Could not decode image"
                    return
                }

                // 3. Rotate bitmap if needed
                val rotatedBitmap = rotateBitmap(originalBitmap, orientation)

                // 4. Center Crop to Square (1:1 Ratio)
                val croppedBitmap = centerCrop(rotatedBitmap)

                // 5. Resize to a standard profile size
                val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 400, 400, true)

                // 6. Compress and convert to Base64
                val outputStream = ByteArrayOutputStream()
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                val base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
                val imageData = "data:image/jpeg;base64,$base64Image"

                updateUserInFirestore(uid, name, bio, imageData)
            } catch (e: Exception) {
                _isLoading.value = false
                _statusMessage.value = "Image processing failed: ${e.localizedMessage}"
            }
        } else {
            updateUserInFirestore(uid, name, bio, _user.value?.profileImageUrl)
        }
    }

    private fun getOrientation(contentResolver: ContentResolver, uri: Uri): Int {
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun centerCrop(srcBmp: Bitmap): Bitmap {
        val width = srcBmp.width
        val height = srcBmp.height
        val size = if (width > height) height else width
        
        val x = (width - size) / 2
        val y = (height - size) / 2
        
        return Bitmap.createBitmap(srcBmp, x, y, size, size)
    }

    private fun updateUserInFirestore(uid: String, name: String, bio: String, imageUrl: String?) {
        val updates = mutableMapOf<String, Any>(
            "name" to name,
            "bio" to bio
        )
        if (imageUrl != null) {
            updates["profileImageUrl"] = imageUrl
        }

        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener {
                _isLoading.value = false
                _statusMessage.value = "Profile updated successfully!"
                fetchUserProfile()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _statusMessage.value = "Update failed: ${e.message}"
            }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
