package com.example.raksha

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private const val ARG_USERNAME = "username"
private const val FINE_PERMISSION_CODE = 1

class HomeFragment : Fragment() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRef: DatabaseReference
    private lateinit var emergencyCallsRef: DatabaseReference
    private lateinit var reportsRef: DatabaseReference
    private lateinit var casesRef: DatabaseReference

    private lateinit var emergencyCallsCountTextView: TextView
    private lateinit var reportsCountTextView: TextView
    private lateinit var casesCountTextView: TextView

    private var homeusername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Database references
        emergencyCallsRef = FirebaseDatabase.getInstance().getReference("emergency_calls")
        reportsRef = FirebaseDatabase.getInstance().getReference("reports")
        casesRef = FirebaseDatabase.getInstance().getReference("cases")

        // Retrieve username from arguments
        arguments?.let {
            homeusername = it.getString(ARG_USERNAME)
        }

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRef = FirebaseDatabase.getInstance().getReference("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize TextViews
        emergencyCallsCountTextView = view.findViewById(R.id.emergencyCallsCountTextView)
        reportsCountTextView = view.findViewById(R.id.reportsCountTextView)
        casesCountTextView = view.findViewById(R.id.casesCountTextView)

        // Retrieve counts from Firebase
        retrieveEmergencyCallsCount()
        retrieveReportsCount()
        retrieveCasesCount()

    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_PERMISSION_CODE)
            return
        }
        val task: Task<Location> = fusedLocationProviderClient.lastLocation
        task.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val location: Location = task.result!!
                val latitude: Double = location.latitude
                val longitude: Double = location.longitude
                Log.e("Location", "Longitude: $longitude, Latitude: $latitude")
                // Save location data to Firebase Realtime Database
                saveLocationToDatabase(latitude, longitude)
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveLocationToDatabase(latitude: Double, longitude: Double) {
        homeusername?.let { username ->
            val userRef: DatabaseReference = locationRef.child(username)
            userRef.child("latitude").setValue(latitude)
            userRef.child("longitude").setValue(longitude)
            Toast.makeText(requireContext(), "Location saved to database", Toast.LENGTH_SHORT).show()


        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun retrieveEmergencyCallsCount() {
        emergencyCallsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get total number of emergency calls
                val emergencyCallsCount: Long = dataSnapshot.childrenCount

                // Update TextView with the count
                emergencyCallsCountTextView.text = emergencyCallsCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve emergency calls count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveReportsCount() {
        reportsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get total number of reports
                val reportsCount: Long = dataSnapshot.childrenCount

                // Update TextView with the count
                reportsCountTextView.text = reportsCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve reports count", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun retrieveCasesCount() {
        casesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get total number of cases
                val casesCount: Long = dataSnapshot.childrenCount

                // Update TextView with the count
                casesCountTextView.text = casesCount.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(requireContext(), "Failed to retrieve cases count", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
