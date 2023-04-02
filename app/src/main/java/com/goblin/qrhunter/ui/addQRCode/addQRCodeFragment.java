package com.goblin.qrhunter.ui.addQRCode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.goblin.qrhunter.MainActivity;
import com.goblin.qrhunter.R;

import com.goblin.qrhunter.Post;
import com.goblin.qrhunter.QRCode;
import com.goblin.qrhunter.data.PostRepository;
import com.goblin.qrhunter.databinding.FragmentAddQrCodeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class addQRCodeFragment extends Fragment {
    private FragmentAddQrCodeBinding binding;
    private addQRCodeViewModel viewModel;
    String TAG = "addQRCodeFragment";

    String qrCode_hash;

    /**
     * Takes in the qr code that was scanned in by the scanner, and sends it to be hashed / generate a post from it.
     * @param qrCode_hash
     * @return Returns the fragment view.
     */
    public static addQRCodeFragment newInstance(String qrCode_hash) {
        addQRCodeFragment fragment = new addQRCodeFragment();
        Bundle args = new Bundle();
        args.putString("qrCode_hash", qrCode_hash);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * This fragment allows the user to add the QR code they scanned into the database -> Reflects in their summary page too.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for fragment.
        binding = FragmentAddQrCodeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(addQRCodeViewModel.class);
        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_add_qr_code, container, false);
        // Get the NavController
//        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.navigation_add_QRCode);
//        NavController navController = navHostFragment.getNavController();
        if (getArguments() != null) {
            qrCode_hash = getArguments().getString("qrCode_hash");
        }
        View root = binding.getRoot();

        // Make a new QR code
        QRCode qrCode = new QRCode(qrCode_hash);
        String qrName = qrCode.NameGenerator();
        String hash = qrCode.getHash();
        Log.d(TAG, "add Qr code: " + hash);
        ImageView avatarImageView = binding.qrRepresentaion;

        Glide.with(this)
                .load("https://api.dicebear.com/6.x/bottts-neutral/png?seed=" + hash)
                .placeholder(R.drawable.baseline_qr_code_50)
                .into(avatarImageView);


        // 1. Bind the qr information with layout
        TextView textView_QR_name = binding.textViewQRname;
        ImageView imageView_QR_representation = binding.qrRepresentaion;
        textView_QR_name.setText(qrName);

        // Bind the visual representation here...
        //imageView_QR_representation.setImageBitmap(qrCode.);

        // Cancel button
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                Toast.makeText(getContext(), "QR code not added.", Toast.LENGTH_SHORT).show();

            }
        });

        // Get the current user (what account to add QR code to).
        binding.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    Post post = new Post(); // This is making an entirely new post object.
                    // Add the qrCode into the post
                    post.setCode(qrCode);
                    post.setPlayerId(userId);
                    post.setName(qrName);

                    // Add the geolocation into the post...

                    // Add the post into the firebase
                    PostRepository postRepo = new PostRepository();
                    postRepo.add(post).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Post added successfully");

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("QR code successfully added");
                            builder.setMessage("Name: " + qrName + '\n' + "Score: " + post.getCode().getScore());
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                // navigate back to homepage
                                public void onClick(DialogInterface dialog, int id) {

                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    startActivity(intent);
                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();

                        } else {
                            Log.e(TAG, "Error adding post", task.getException());
                        }

                    });

                }
            }
        });


        // Create a new instance of HomeViewModel
        // viewModel = new ViewModelProvider(this).get(confirmAddQRCodeViewModel.class);
        return binding.getRoot();
    }
}