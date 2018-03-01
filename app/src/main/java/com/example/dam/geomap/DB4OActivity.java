package com.example.dam.geomap;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

import java.io.IOException;
import java.util.GregorianCalendar;

/*
* Meter librería en Project/GeoMap/app/libs
* Botón derecho en el archivo jar y Add as library
*/

public class DB4OActivity extends AppCompatActivity {

    private static final String TAG = DB4OActivity.class.getSimpleName();

    private ObjectContainer objectContainer;

    public EmbeddedConfiguration getDb4oConfig() throws IOException {
        EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        //Dar soporte para android
        configuration.common().add(new AndroidSupport());
        //Indexar fechas
        configuration.common().objectClass(Localizacion.class).
                objectField("fecha").indexed(true);
        return configuration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db4_o);

        startService(new Intent(this, LocationService.class));

        objectContainer = openDataBase("ejemplo.db4o");

        Localizacion loc = new Localizacion();
        objectContainer.store(loc);
        objectContainer.commit();

        loc = new Localizacion(new Location("provider"));
        objectContainer.store(loc);
        objectContainer.commit();

        loc = new Localizacion(new Location("proveedor"), new GregorianCalendar(2018,1,22).getTime());
        objectContainer.store(loc);
        objectContainer.commit();

        //Encontrar todas las localizaciones que hay
        Query consulta = objectContainer.query();
        consulta.constrain(Localizacion.class);
        ObjectSet<Localizacion> localizaciones = consulta.execute();
        for(Localizacion localizacion: localizaciones){
            Log.v(TAG, "1: " + localizacion.toString());
        }

        //Creo una consulta y le asocio un predicado. El predicado debe ser true
        //En este ej buscar todos los objetos de una fecha dada
        ObjectSet<Localizacion> locs = objectContainer.query(
                new Predicate<Localizacion>() {
                    @Override
                    public boolean match(Localizacion loc) {
                        return loc.getFecha().equals(new GregorianCalendar(2018,1,22).getTime());
                    }
                });
        for(Localizacion localizacion: locs){
            Log.v(TAG, "2: " + localizacion.toString());
        }
        objectContainer.close();
    }

    //Abre la conexión con la bbdd
    private ObjectContainer openDataBase(String archivo) {
        ObjectContainer objectContainer = null;
        try {
            //Crear en la carpeta externa privada
            String name = getExternalFilesDir(null) + "/" + archivo;
            objectContainer = Db4oEmbedded.openFile(getDb4oConfig(), name);
        } catch (IOException e) {
            Log.v(TAG, e.toString());
        }
        return objectContainer;
    }
}
