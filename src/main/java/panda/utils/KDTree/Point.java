package panda.utils.KDTree;

import lombok.Data;

/**
* @author xiang.zhang@chelaile.net.cn
* @date   2019年4月26日
*/

@Data
public class Point  {


    public int getIndex() {
        return index;
    }

    //KDTree 标记是第几个元素
    private   int index = -1;
    private double lng;
    private double lat;

    public Point(double lng, double lat) {

        this.lng = lng;
        this.lat = lat;
    }
    public Point(Point point, int index) {
        this.lng = point.getLng();
        this.lat = point.getLat();
        this.index = index;
    }
    public Point(Point point) {
        this.lng = point.getLng();
        this.lat = point.getLat();
    }

    public Point(String pointString) {
        String point[] = pointString.split(",");
        this.lng = Double.parseDouble(point[0]);
        this.lat = Double.parseDouble(point[1]);
    }

    public Point() {
        this.lng = -1 ;
        this.lat = -1 ;
    }

    @Override
    public String toString() {
        return "Point{" + lng +
                "," + lat +
                "," + index +
                '}';
    }


    public String toCoordinate() {
        return  lng + "," + lat ;
    }
    public String toCoordinate2() {
        return String.format("%.6f,%.6f;", lng, lat);
    }
}
