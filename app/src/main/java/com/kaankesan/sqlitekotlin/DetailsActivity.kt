package com.kaankesan.sqlitekotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_details.*
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {

    private lateinit var  art_db :SQLiteDatabase
    private lateinit var byteArray : ByteArray
    private lateinit var launcher : ActivityResultLauncher<Intent>
    private lateinit var permission : ActivityResultLauncher<String>
    private var selectedBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        art_db = this.openOrCreateDatabase("Database", MODE_PRIVATE,null)

        register()

        val intent = intent

        val set = intent.getStringExtra("idIx")

        if(set.equals("new")){

            NameIx.setText("")
            ArtistIx.setText("")
            imageView.setImageResource(R.drawable.select)

        }else{
            val id =  intent.getIntExtra("Id",1)

            val cursor = art_db.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(id.toString()))

            val artnameIx =  cursor.getColumnIndex("artname")
            val artistIx = cursor.getColumnIndex("artist")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){

                NameIx.setText(cursor.getString(artnameIx))
                ArtistIx.setText(cursor.getString(artistIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)

                imageView.setImageBitmap(bitmap)

            }

            cursor.close()
        }



        button.setOnClickListener {

            val Name = NameIx.text.toString()
            val artist = ArtistIx.text.toString()

            if(selectedBitmap != null){
                val smallBitmap = makeSmaller(selectedBitmap!!,300)
                val outputStream = ByteArrayOutputStream()
                smallBitmap.compress(Bitmap.CompressFormat.PNG,80,outputStream)
                 byteArray = outputStream.toByteArray()
            }

            try {
                //val art_db = openOrCreateDatabase("Database", MODE_PRIVATE,null)
                art_db.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,artist VARCHAR,image BLOB) ")

                val SqlString = "INSERT INTO arts (artname,artist,image) VALUES (?,?,?)"
                val statement = art_db.compileStatement(SqlString)
                statement.bindString(1,Name)
                statement.bindString(2,artist)
                statement.bindBlob(3, byteArray)
                statement.execute()


            }catch (e:Exception){

            }

            val intent = Intent(this@DetailsActivity,MainActivity::class.java)
            startActivity(intent)
            finish()

        }

        imageView.setOnClickListener {

            if(ContextCompat.checkSelfPermission(this@DetailsActivity,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(it,"Permission needed",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission"
                    ) {
                        permission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }.show()
                }else{
                    permission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(intentToGallery)
            }

        }


    }

    private fun makeSmaller(bitmap: Bitmap, maxSize:Int):Bitmap {

        var height = bitmap.height
        var width = bitmap.width

        val ratio :Double = width.toDouble() / height.toDouble()

        if(ratio > 1){
            width = maxSize
            val scaled = width/ratio
            height = scaled.toInt()

        }else{

            height = maxSize
            val scaled = height/ratio
            width = scaled.toInt()
        }

        return Bitmap.createScaledBitmap(bitmap,width,height,true)
    }

    private fun register(){

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->

            if(result.resultCode == RESULT_OK){

                val intentFromData = result.data
                if(intentFromData != null){
                    val image = intentFromData.data

                    if(image != null){
                        try {
                            val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver,image)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(selectedBitmap)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                }


            }

        }

        permission = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->

            if(result){

                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(intentToGallery)

            }else{

                Toast.makeText(this@DetailsActivity,"Permission denied",Toast.LENGTH_SHORT).show()
            }

        }

    }
}