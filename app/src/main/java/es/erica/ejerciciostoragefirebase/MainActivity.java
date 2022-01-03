package es.erica.ejerciciostoragefirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Button btnSeleccionarImagen;
    private  Button btnSubirImagen;
    private ImageView previewImagenSeleccionada;
    private ProgressBar progressBar;
    //Imagenes
    private static final int SELECT_FILE = 1;
    private Uri uriImagen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniciarlizarVistas();
    }

    private void iniciarlizarVistas(){
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnSubirImagen = findViewById(R.id.btnSubirImagen);
        previewImagenSeleccionada = findViewById(R.id.ivPreview);
        progressBar = findViewById(R.id.progressBar);

        btnSeleccionarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirGaleria();
            }
        });

        btnSubirImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uriImagen!=null){
                    subirImagenStorage();
                }else{
                    Toast.makeText(getApplicationContext(), "Selecciona una imagen",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*Abrir la galeria*/
    private void abrirGaleria(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(intent, "Seleccione una imagen"),
                SELECT_FILE);
    }

    /*Evento del ciclo de vida que recoge el resultado de una acción posterior*/
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Uri selectedImage;

        String filePath = null;
        switch (requestCode) {
            case SELECT_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = imageReturnedIntent.getData();
                    String selectedPath=selectedImage.getPath();
                    uriImagen = selectedImage;
                    if (selectedPath != null) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(
                                    selectedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        // Transformamos la URI de la imagen a inputStream y este a un Bitmap
                        Bitmap bmp = BitmapFactory.decodeStream(imageStream);
                        btnSubirImagen.setEnabled(true);

                        // Ponemos nuestro bitmap en un ImageView que tengamos en la vista
                        previewImagenSeleccionada.setImageBitmap(bmp);

                    }
                }
                break;
        }
    }
    /*Subir imagen a Firebase Storage*/
    private void subirImagenStorage(){

        progressBar.setVisibility(View.VISIBLE);
        btnSubirImagen.setEnabled(false);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        StorageReference riversRef = storageRef.child(uriImagen.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(uriImagen);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), "Ocurrió un error al subir la imagen",
                            Toast.LENGTH_LONG).show();
                    //Mostramos el progress bar y bloqueamos el boton para no clicar más veces
                    progressBar.setVisibility(View.GONE);
                    btnSubirImagen.setEnabled(true);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(getApplicationContext(), "Imagen Subida a Firebase Storage.",
                            Toast.LENGTH_LONG).show();
                    //Mostramos el progress bar y bloqueamos el boton para no clicar más veces
                    progressBar.setVisibility(View.GONE);
                    btnSubirImagen.setEnabled(true);
                }
            });
    }

}