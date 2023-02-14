package com.example.losting20.ui;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.os.Build.VERSION.SDK_INT;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.losting20.Incidencia;
import com.example.losting20.R;
import com.example.losting20.SharedViewModel;
import com.example.losting20.databinding.FragmentAddReportBinding;
import com.example.losting20.ui.home.HomeViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddReport extends Fragment {

    private FragmentAddReportBinding binding;

    private FirebaseUser authUser;
    public static int RESULT_LOAD_IMG=2;
    public StorageReference storageRef;
    private String randomName;
    private StorageReference ref;
    private Uri photoURI;
    private String calle;
    private Boolean bimgen = false;


    public AddReport() {

    }

    public static AddReport newInstance(String param1, String param2) {
        AddReport fragment = new AddReport();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentAddReportBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.getCurrentLatLng().observe(getViewLifecycleOwner(), latlng -> {
            binding.txtLatitud.setText(String.valueOf(latlng.latitude));
            binding.txtLongitud.setText(String.valueOf(latlng.longitude));

            binding.txtDireccio.setText(fetchAddress());
        });

        sharedViewModel.switchTrackingLocation();

        sharedViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            authUser = user;
        });
        binding.buttonNotificar.setOnClickListener(button -> {
            if(bimgen == true){
                Incidencia incidencia = new Incidencia();
                incidencia.setDireccio(fetchAddress().toString());
                incidencia.setLatitud(binding.txtLatitud.getText().toString());
                incidencia.setLongitud(binding.txtLongitud.getText().toString());
                incidencia.setProblema(binding.txtDescripcion.getText().toString());
                incidencia.setUrl(randomName);
                DatabaseReference base = FirebaseDatabase.getInstance(
                ).getReference();

                DatabaseReference users = base.child("users");
                DatabaseReference uid = users.child(authUser.getUid());
                DatabaseReference incidencies = uid.child("reportes");

                DatabaseReference reference = incidencies.push();
                reference.setValue(incidencia);
                uploadImag();
                NavHostFragment.findNavController(AddReport.this).navigate(R.id.action_addReport_to_navigation_home);
                Toast.makeText(getContext(), "Notificado correctamente", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(getContext(), "No escogiste ninguna imagen", Toast.LENGTH_LONG).show();
            }
        });

        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AddReport.this).navigate(R.id.action_addReport_to_navigation_home);
            }
        });
        binding.buttonFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //requestPermission();
                bimgen = true;
                loadimageFromGallery();
            }
        });
    }
    //PERMISOS GALERIA
    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getActivity().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {
            //below android 11
            int PERMISSION_REQUEST_CODE = 1;
            ActivityCompat.requestPermissions(getActivity(), new String[]{WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    //Galeria
    public void loadimageFromGallery(){
            //Create intent
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            //Start Intent
            startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            storageRef = storage.getReferenceFromUrl("gs://losting2-0.appspot.com/");
            randomName = UUID.randomUUID().toString();
            Log.e("NAMENAME",randomName);
            ref = storageRef.child("publicaciones/"+randomName);

            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContext().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                String imgDecodableString = cursor.getString(columnIndex);

                String mCurrentPhotoPath = cursor.getString(columnIndex);

                File uriFile = new File(mCurrentPhotoPath);
                photoURI = Uri.fromFile(uriFile);
                storageRef.putFile(photoURI);

                Log.e("PATH" , cursor.getString(columnIndex));

                cursor.close();

                ImageView iv = getView().findViewById(R.id.ivHold);
                // Set the Image in ImageView after decoding the String
                iv.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));

            } else {
                Toast.makeText(getContext(), "No escogiste ninguna imagen",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Algo ha salido mal", Toast.LENGTH_LONG)
                    .show();
            Log.e("ERROR URI", e+"");
        }
    }

    private String fetchAddress() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        Geocoder geocoder = new Geocoder(requireContext(),
                Locale.getDefault());

        executor.execute(() -> {
            // Aquest codi s'executa en segon pla
            List<Address> addresses = null;
            String resultMessage = "";

            try {

                addresses = geocoder.getFromLocation(
                        Double.parseDouble(String.valueOf(binding.txtLatitud.getText())),
                        Double.parseDouble(String.valueOf(binding.txtLongitud.getText())),
                        1);

                if (addresses == null || addresses.size() == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = "No s'ha trobat cap adreça";
                        Log.e("INCIVISME", resultMessage);
                    }
                } else {


                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();

                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressParts.add(address.getAddressLine(i));
                    }
                    calle = addresses.get(0).getAddressLine(0);
                }

            } catch (IOException ioException) {
                resultMessage = "Servei no disponible";
                Log.e("INCIVISME", resultMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                resultMessage = "Coordenades no vàlides";
                Log.e("INCIVISME", resultMessage + ". " +
                        "Latitude = " + binding.txtLatitud.getText().toString() +
                        ", Longitude = " +
                        binding.txtLongitud.getText(), illegalArgumentException);
            }
        });
        return calle;
    }

    private void uploadImag(){
        ref.putFile(photoURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef = ref;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(),"Failed " + e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                ProgressDialog progressDialog = new ProgressDialog(getContext());
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int)progress + "%");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}