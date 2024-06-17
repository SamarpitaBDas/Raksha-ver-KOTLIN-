package com.example.raksha

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

private const val MY_PERMISSIONS_REQUEST_CALL_PHONE = 1
private const val TAG = "EmergencyFragment"

class EmergencyFragment : Fragment() {

    private lateinit var facultyLocations: MutableMap<String, Location>
    private lateinit var databaseReference: DatabaseReference

    private var emergencyusername: String? = null
    private var latitude_em: Double = 0.0
    private var longitude_em: Double = 0.0

    private lateinit var faculty_call: Button
    private lateinit var call_police: Button
    private lateinit var call_hospital: Button
    private lateinit var call_womenhp: Button
    private lateinit var call_womencommision: Button
    private lateinit var call_women_honor_call: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        facultyLocations = mutableMapOf()
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        fetchUserArguments()
        populateFacultyLocations()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emergency, container, false)

        // Initialize buttons
        faculty_call = view.findViewById(R.id.faculty_call)
        call_police = view.findViewById(R.id.call_police)
        call_hospital = view.findViewById(R.id.call_hospital)
        call_womenhp = view.findViewById(R.id.call_womenhp)
        call_womencommision = view.findViewById(R.id.call_womencommision)
        call_women_honor_call = view.findViewById(R.id.call_women_honor_call)

        
        return view
    }

    private fun fetchUserArguments() {
        emergencyusername = arguments?.getString("username")
    }

    private fun populateFacultyLocations() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val userData = userSnapshot.value as? Map<String, Any>
                    if (userData != null && "faculty" == userData["role"]) {
                        val username = userSnapshot.key
                        val latitude = userData["latitude"] as Double
                        val longitude = userData["longitude"] as Double
                        facultyLocations[username!!] = Location(username).apply {
                            this.latitude = latitude
                            this.longitude = longitude
                        }
                        Log.d(TAG, "Faculty location added: Username = $username, Latitude = $latitude, Longitude = $longitude")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Error retrieving faculty locations: ${databaseError.message}")
            }
        })
    }

    private fun setButtonClickListeners() {
        faculty_call.setOnClickListener {
            val currentUserLocation = getCurrentUserLocation(latitude_em, longitude_em)
            if (currentUserLocation != null) {
                val nearestFaculty = findNearestFaculty(currentUserLocation)
                nearestFaculty?.let {
                    getFacultyNumber(it, object : FacultyNumberCallback {
                        override fun onCallback(phoneNumber: String?) {
                            if (!phoneNumber.isNullOrBlank()) {
                                makePhoneCall(phoneNumber)
                            } else {
                                Toast.makeText(requireContext(), "Unable to retrieve faculty phone number", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                } ?: run {
                    Toast.makeText(requireContext(), "No faculty found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Unable to retrieve user location", Toast.LENGTH_SHORT).show()
            }
        }

        call_police.setOnClickListener {
            makePhoneCall("100")
        }

        call_hospital.setOnClickListener {
            makePhoneCall("112")
        }

        call_womenhp.setOnClickListener {
            makePhoneCall("1090")
        }

        call_womencommision.setOnClickListener {
            makePhoneCall("9454401122")
        }

        call_women_honor_call.setOnClickListener {
            makePhoneCall("5222614978")
        }
    }

    private fun getFacultyNumber(it: String, facultyNumberCallback: EmergencyFragment.FacultyNumberCallback) {

    }

    private fun makePhoneCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), MY_PERMISSIONS_REQUEST_CALL_PHONE)
        } else {
            val dial = "tel:$phoneNumber"
            startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
            Log.d(TAG, "Making phone call to: $phoneNumber")
            saveEmergencyCallToDatabase(emergencyusername, latitude_em, longitude_em)
        }
    }

    private fun saveEmergencyCallToDatabase(username: String?, latitude: Double, longitude: Double) {
        val emergencyCallsRef = FirebaseDatabase.getInstance().getReference("emergency_calls")
        val callId = emergencyCallsRef.push().key // Generate a unique key for the emergency call
        callId?.let {
            val emergencyCallData = hashMapOf<String, Any>(
                "username" to username!!,
                "latitude" to latitude,
                "longitude" to longitude
            )
            emergencyCallsRef.child(callId).setValue(emergencyCallData)
            Log.d(TAG, "Emergency call data saved to database")
        } ?: run {
            Log.e(TAG, "Failed to generate a key for the emergency call")
        }
    }

    private fun getCurrentUserLocation(latitude: Double, longitude: Double): Location {
        val mockLocation = Location("mock")
        mockLocation.latitude = latitude
        mockLocation.longitude = longitude
        Log.d(TAG, "Current user location: Latitude = $latitude, Longitude = $longitude")
        return mockLocation
    }

    private fun findNearestFaculty(currentUserLocation: Location): String? {
        var nearestFaculty: String? = null
        var minDistance = Float.MAX_VALUE

        for ((username, facultyLocation) in facultyLocations) {
            val distance = currentUserLocation.distanceTo(facultyLocation)
            if (distance < minDistance) {
                minDistance = distance
                nearestFaculty = username
            }
        }
        Log.d(TAG, "Nearest faculty: $nearestFaculty")
        return nearestFaculty
    }
    
    interface FacultyNumberCallback {
        fun onCallback(phoneNumber: String?)
    }
}
