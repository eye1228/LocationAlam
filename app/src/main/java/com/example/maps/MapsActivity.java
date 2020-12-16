package com.example.maps;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMarkerClickListener {
    private int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    private GoogleMap mMap;
    int a = 0; //시작인지 아닌지 판단
    int b = 0;// 목표 지점 지정 됬는지 아닌지 판단
    int g_btn=0;//목적지 고정 했는지 안했는지. 0은 고정 안함, 1은 고정 함.
    double loc = 0;
    double lat = 0;
    double gloc = 0;
    double glat = 0;
    LatLng past = new LatLng(loc, lat);
    Marker mGOAL;
    Marker mNow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);//GPS에 접속을 위한 허가 요청

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final Button button = (Button)findViewById(R.id.goal_btn);

        button.setOnClickListener(new View.OnClickListener(){//목적지 고정관련 버튼 클릭시 호출
            public void onClick(View v)
            {
                if(g_btn == 0)//고정 안되있을 때
                {
                    g_btn = 1;
                    button.setText("목적지 고정 해제");
                }
                else if(g_btn == 1)//고정 되어 있을 때.
                {
                    g_btn = 0;
                    button.setText("목적지 고정 하기");
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MapsActivity.this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }
        mMap.setMyLocationEnabled(true);//내 위치 허용
        //클릭시 마커 생성 - 목표지점 마커.
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            public void onMapClick(LatLng point)
            {
                if(g_btn == 0)
                {
                    gloc = point.latitude;
                    glat = point.longitude;
                    String sGoalloction = "위도: " + Double.toString(gloc) + " 경도: " + Double.toString(glat);

                    LatLng goal  = new LatLng(gloc, glat);

                    MarkerOptions makerOptions = new MarkerOptions();
                    makerOptions.position(goal).title("도착지점").snippet(sGoalloction).icon(BitmapDescriptorFactory.fromResource(R.drawable.goal));//마커에 위도 경도 저장
                    if(b == 1) //목표가 이미 찍혔으면
                    {
                        mGOAL.remove();//목표 마커 삭제
                        mGOAL = mMap.addMarker(makerOptions);//목표 마커 다시 그리기.
                    }
                    else{
                        mGOAL = mMap.addMarker(makerOptions);
                        b=1;
                    }
                }
            }
        });


        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        LocationListener locationListener = new LocationListener() {//좌표 변경시 작동
            @Override
            public void onLocationChanged(Location location) {
                LatLng now = new LatLng(location.getLatitude(), location.getLongitude());

                MarkerOptions makerOptions = new MarkerOptions();
                makerOptions.position(now).title("현재위치").icon(BitmapDescriptorFactory.fromResource(R.drawable.k));//현재 위치 마커 관련
                if(a==0)//처음 시작할 때만. 시작점 마커를 찍는다.
                {
                    mNow = mMap.addMarker(makerOptions);//현재 위치 마커 그리기
                    LatLng Start = new LatLng(location.getLatitude(), location.getLongitude());
                    String sStartloction = "위도: " + Double.toString(location.getLatitude()) + " 경도: " + Double.toString(location.getLongitude());
                    past = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(Start).title("시작지점").snippet(sStartloction));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(Start));
                    a++;
                }
                else//처음 시작이 아님, 그림 그려지는 곳.
                {

                    mNow.remove();//현재 위치 마커 지우기
                    mNow = mMap.addMarker(makerOptions);//현재 위치 마커 다시그리기

                    mMap.addPolyline(new PolylineOptions().add(past,now));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(now));
                    past = now;
                    Toast.makeText(MapsActivity.this, getCurrentAddress(location.getLatitude(), location.getLongitude()), Toast.LENGTH_LONG).show();
                    if(b == 1){ //목표지점이 찍혀 있을 때.
                        double glocRound = Math.round((gloc*10000))/10000.0;//목적지와 현재위치의 위도 경도 계산(소숫점 3자리까지)
                        double glatRound = Math.round((glat*10000))/10000.0;
                        double nlocRound = Math.round((location.getLatitude()*10000))/10000.0;
                        double nlatRound = Math.round((location.getLongitude()*10000))/10000.0;

                        if(glocRound == nlocRound && glatRound == nlatRound)//목표에 접근하면
                        {
                            Intent intent = new Intent(getApplicationContext(), alarm.class);//알람 class로 이동
                            startActivity(intent);
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
            }
         };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);//재업
    }
    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        Toast.makeText(MapsActivity.this, marker.getTitle(), Toast.LENGTH_LONG).show();
        return false;
    }

    public String getCurrentAddress( double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }
}
