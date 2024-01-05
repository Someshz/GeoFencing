package com.example.geofencingproject

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddLocation : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_add_location, container, false)
        return view;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = view.findViewById<Button>(R.id.submit)
        val latText = view.findViewById<EditText>(R.id.latET)
        val lagText = view.findViewById<EditText>(R.id.lagET)
        val radius = view.findViewById<EditText>(R.id.radiusET)


     //   Log.d("Hello ",latitude.toString()+" "+longitude.toString()+" "+radiusText.toString()+" "+isSubmit)
        button.setOnClickListener(View.OnClickListener {

            val latitude = latText.text.toString().toDoubleOrNull()
            val longitude = lagText.text.toString().toDoubleOrNull()
            val radiusText = radius.text.toString().toFloatOrNull()

            val isSubmit = latitude != null && longitude != null && radiusText != null &&
                    latitude >= -90.0 && latitude <= 90.0 &&
                    longitude >= -180.0 && longitude <= 180.0 &&
                    radiusText in 10.0..1000.0

            if(isSubmit) {
                (activity as MainActivity).addDataToList(
                    GeoLocation(
                        latText.text.toString(),
                        lagText.text.toString(),
                        radius.text.toString()
                    )
                )
                (activity as MainActivity).onBackPressed()
            }else{
                val builder =
                    AlertDialog.Builder(context)
                builder.setTitle("Wrong Input")
                builder.setMessage("Please enter correct latitude and longitude and radius")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    it.dismiss()
                }
                builder.show()
            }
        })
    }

}