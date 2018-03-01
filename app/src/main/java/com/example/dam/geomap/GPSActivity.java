package com.example.dam.geomap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class GPSActivity extends AppCompatActivity {

    private static final String TAG = "xyz";
    private static final int PERMISO_LOCATION = 1;
    private static final int RESOLVE_RESULT = 2;

    private FusedLocationProviderClient clienteLocalizacion;
    private LocationCallback callbackLocalizacion;
    private LocationRequest peticionLocalizacion;
    private LocationSettingsRequest ajustesPeticionLocalizacion;
    private SettingsClient ajustesCliente;

    private boolean checkPermissions() {
        int estadoPermisos = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return estadoPermisos == PackageManager.PERMISSION_GRANTED;
    }

    private void init() {
        if(checkPermissions()) {
            //startService();
            startLocations();
        } else {
            requestPermissions();
        }
    }

    //Método que se ejecuta automáticamente cuando vengo de otra actividad
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESOLVE_RESULT:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.v(TAG, "Permiso ajustes localización");
                        startLocations();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.v(TAG, "Sin permiso ajustes localización");
                        break;
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        init();
    }

    //
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && requestCode == PERMISO_LOCATION) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocations();
            }
        }
    }

    private void requestPermissions() {
        //Mira si tengo que solicitar un permiso de forma racional.
        //Dar razón adicional al usuario, sólo si el usuario ha sido requerido para dar permisos, no los dio y marcó no dar de nuevo
        boolean solicitarPermiso = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        //Si se cumple. Ha rechazado pero no ha marcado no preguntar más
        //La primera vez no sale la casilla de volver a preguntar
        if (solicitarPermiso) {
            Log.v(TAG, "Explicación racional del permiso");
            showSnackbar(R.string.app_name, android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Solicitar permiso
                    ActivityCompat.requestPermissions(GPSActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISO_LOCATION);
                }
            });
        } else {
            Log.v(TAG, "Solicitando permiso");
            //lanzar solicitud de permiso
            ActivityCompat.requestPermissions(GPSActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_LOCATION);
        }
    }

    private void showSnackbar(final int idTexto, final int textoAccion,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(idTexto),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(textoAccion), listener).show();
    }

    @SuppressLint("MissingPermission")
    private void startLocations() {
        //Objeto que va a obtener las diferentes localizaciones a lo largo del camino
        clienteLocalizacion = LocationServices.getFusedLocationProviderClient(this);
        //Clase donde específico de qué forma quiero obtener esas localizaciones
        ajustesCliente = LocationServices.getSettingsClient(this);

        //Obtengo última localización conocida
        clienteLocalizacion.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.v(TAG, "última localización: " + location.toString());
                } else {
                    Log.v(TAG, "no hay última localización");
                }
            }
        });

        callbackLocalizacion = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //Aquí es donde se van obteniendo las localizaciones
                Location localizacion = locationResult.getLastLocation();
                Log.v(TAG, localizacion.toString());
            }
        };
        //Preparar objeto con el que voy a lanzar la geolocalización
        peticionLocalizacion = new LocationRequest();
        peticionLocalizacion.setInterval(10000);
        peticionLocalizacion.setFastestInterval(5000);
        peticionLocalizacion.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Máxima precisión posible en las geolocalizaciones

        //Contruir y lanzar el cliente de peticiones
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(peticionLocalizacion);
        ajustesPeticionLocalizacion = builder.build();

        ajustesCliente.checkLocationSettings(ajustesPeticionLocalizacion)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.v(TAG, "Se cumplen todos los requisitos");
                        //Lanzo mi cliente de geolocalización, que cuando tenga nuevas geolocalizaciones me las entrega a través de callback anterior
                        clienteLocalizacion.requestLocationUpdates(peticionLocalizacion, callbackLocalizacion, null);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.v(TAG, "Falta algún requisito, intento de adquisición");
                                try {
                                    //Intenta subsanar el error | onActivityResult
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(GPSActivity.this, RESOLVE_RESULT);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.v(TAG, "No se puede adquirir.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.v(TAG, "Falta algún requisito, que no se puede adquirir."); //No es subsanable
                        }
                    }
                });
    }
}
