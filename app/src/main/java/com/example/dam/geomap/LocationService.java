package com.example.dam.geomap;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Created by dam on 21/02/2018.
 */

public class LocationService extends Service {

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Intent i=new Intent(this, LocationService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //Creando notificación con dibujo y texto
        Notification.Builder constructorNotificacion = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("notificación servicio")
                .setContentText("texto servicio")
                .setContentIntent(PendingIntent.getActivity(this, 0, i, 0));
        //Convertir mi servicio a uno foreground
        startForeground(1, constructorNotificacion.build());
    }

    //Ejecutar una vez creado el servicio
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //do the job ... iniciar geolocalización, obtenerlas y guardar cada una en la bbdd
        return START_STICKY;
    }
}
