package panda.utils.KDTree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import panda.utils.Util;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class KDTree {
    public static final double PI    = 3.1415926;
    public static final double r     = 6378137;   // 地球的赤道半径
    public static final double delta = 1e-7;      // GPS点的精度

    static double degToRad = PI / 180.0;
    static double radToDeg = 180.0 / PI;


    //需要查询的点
    private ThreadLocal<Point> queryPoint = new ThreadLocal<>();
    //private Point queryPoint;new PriorityQueue<>(new PointComparator());
    private ThreadLocal<Queue<Point> > pointQueue =  new ThreadLocal<>();
    //查询的最近数量
    private ThreadLocal<Integer> findQuantity = new ThreadLocal<>();

    private double findRangeLen;

    private int nodeCount = 0;

    private List<Node> kdTree;

    private List<Point> points;
    private List<Double> queryDistances;

    public List<Point> getPoints() {
        return points;
    }
    //创建大顶堆

    /**
     * 创建大根堆的自定义对比大小
     */
    class PointComparator implements Comparator<Point> {
        public int compare(Point pointX, Point pointY) {
            //计算需要查询的Point的距离   ，queryPoint为外部输入需要查询的点
            double disX = calculateDistance(pointX, queryPoint.get());
            double disY = calculateDistance(pointY, queryPoint.get());
            if (disX <= disY) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }



    static Logger log = LoggerFactory.getLogger(KDTree.class);
    private static Util util = new Util();

    //初始化构建kdTree
    public KDTree(List<InterfaceKDTree> objects ) {
        List<Point> points =  new ArrayList<>();
        //log.info("{} " , objects.size());
        for(InterfaceKDTree object : objects){
            if(object instanceof  InterfaceKDTree){
                points.add(   new Point(object.getLng() , object.getLat()) );
            }else{
                log.error("传入数据类型错误");
                break;
            }
        }
        buildKDTree(points);
    }

    //初始化构建kdTree
    public KDTree(List<Point> points ,Point point) {
        buildKDTree(points);
    }

    /**
     * 外部传入参数建造kdTree
     *
     * @param points 传入的需要构建KDTree 的坐标
     */
    public void buildKDTree(List<Point> points) {

        this.kdTree = new ArrayList<>();
        for (int i = 0; i < points.size() + 10; i++) {
            Node node = new Node();
            this.kdTree.add(node);
        }
        //log.debug("kdTree_node_size =={} ", kdTree.size());
        this.points =  new ArrayList<>();
        List<Point> buildTreePoints = new ArrayList<>();
        for(int i = 0 ; i < points.size() ; i++){
            //log.info("{} " ,points.get(i) );
            Point point = new Point(points.get(i) , i);
            this.points.add(point);
            buildTreePoints.add(point);
        }
        this.buildKDTree(true, buildTreePoints);
    }


    /**
     * 根据不同维度进行排序
     *
     * @param split
     * @param points
     */
    private void sortPoint(boolean split, List<Point> points) {
        if (split) {
            points.sort(Comparator.comparing(Point::getLng));
        } else {
            points.sort(Comparator.comparing(Point::getLat));
        }
    }

    /**
     * 新建List<Point> 方便进行k纬度排序
     *
     * @param left
     * @param right
     * @param points
     * @return
     */
    private List<Point> getNewPoints(int left, int right, List<Point> points) {
        List<Point> newPoints = new ArrayList<>();
        for (int i = left; i < right; i++) {
            newPoints.add(points.get(i));
        }
        return newPoints;
    }

    /**
     * 建造kdTree树
     *
     * @param split  传入分割纬度
     * @param points 传入列表
     * @return
     */
    private int buildKDTree(boolean split, List<Point> points) {
        if (points.isEmpty()) {
            return 0;
        }
        //根据纬度排序
        this.sortPoint(split, points);

        //指针后移
        int ret = this.nodeCount++;
        int middle = points.size() / 2;

        List<Point> leftPoints = getNewPoints(0, middle, points);
        List<Point> rightPoints = getNewPoints(middle + 1, points.size(), points);
        //log.debug(" points {} leftPoints {}  rightPoints {}  point {} ", points.size(), leftPoints.size(), rightPoints.size(), points.get(middle));
        //节点上添加详细信息
        kdTree.get(ret).point = points.get(middle);
        kdTree.get(ret).ls = buildKDTree(!split, leftPoints);
        kdTree.get(ret).rs = buildKDTree(!split, rightPoints);

        return ret;
    }


    /**
     * 估算距离大小的函数，sqrt太耗时 所以不开方
     *
     * @param x
     * @param y
     * @return
     */
    public double calculateDistance(Point x, Point y) {

        // return  Util.distance(x ,y);

        double xAxis = x.getLng() - y.getLng();
        double yAxis = x.getLat() - y.getLat();
        double distance = xAxis * xAxis + yAxis * yAxis;
        return distance;

    }
    /**
     * 记录最近的findQuantity个结果
     *
     * @param point
     */
    private void pushNearestPoint(Point point) {
        //log.info("pointQueue {}  {}  " , pointQueue.size() , this.findQuantity);
        pointQueue.get().offer(point);
        if (pointQueue.get().size() > this.findQuantity.get()   ) {
            pointQueue.get().poll();
        }
    }

    private void findNearest(int index, boolean split) {

        int nextLeft = this.kdTree.get(index).ls;
        int nextRight = this.kdTree.get(index).rs;

        if (split) {
            if (!(queryPoint.get().getLng() < kdTree.get(index).point.getLng())) {
                int temp = nextLeft;
                nextLeft = nextRight;
                nextRight = temp;
            }
        } else {
            if (!(queryPoint.get().getLat() < kdTree.get(index).point.getLat())) {
                int temp = nextLeft;
                nextLeft = nextRight;
                nextRight = temp;
            }
        }
        //log.info("index {}  {}   {} " , index ,nextLeft , nextRight  );
        if (nextLeft > 0) {
            this.findNearest(nextLeft, !split);
        }

        this.pushNearestPoint(kdTree.get(index).point);

        //判断圆与切割线是否存在交点
        double dis = 0;

        if (split) {
            dis = calculateDistance(new Point(queryPoint.get().getLng()  , kdTree.get(index).point.getLat() )  , new Point( kdTree.get(index).point.getLng() ,  kdTree.get(index).point.getLat() ) );
        } else {
            dis = calculateDistance(new Point( kdTree.get(index).point.getLng() , queryPoint.get().getLat()  )  , new Point(  kdTree.get(index).point.getLng(),  kdTree.get(index).point.getLat() ) );
        }
        //另外一个分支可能存在更近的点
        if (nextRight > 0 && (calculateDistance(pointQueue.get().peek(), queryPoint.get()) >= dis
                //|| pointQueue.size() < this.findQuantity
        )) {
            this.findNearest(nextRight, !split);
        }
    }


    /**
     * 查询findQuantity个距离queryPoint最近的点
     *
     * @param queryPoint
     * @param findQuantity
     * @return
     */
    public List<Point> getNearestPoints(Point queryPoint, int findQuantity) {
        /*
        queryDistances = new ArrayList<>();
        for(int i = 0 ; i < this.points.size() ; ++i){
            Point point2 = this.points.get(i);
            double distance = Util.distance(point2 ,queryPoint );
            queryDistances.add( distance);
        }
         */

        pointQueue.set(new PriorityQueue<>(new PointComparator())) ;

        this.queryPoint.set(queryPoint) ;
        this.findQuantity.set(findQuantity);

        this.findNearest(0, true);

        List<Point> newPoints = new ArrayList<>();

        while (!pointQueue.get().isEmpty()) {
            newPoints.add(pointQueue.get().peek());
            double dis = util.distance(queryPoint, pointQueue.get().peek());
            //log.info("大顶堆记录  {} {}  {}  {}  ", dis,  String.format( "%.6f",calculateDistance(queryPoint, pointQueue.peek() ) ),queryPoint, pointQueue.peek());
            pointQueue.get().poll();
        }
        Collections.reverse(newPoints);
        return newPoints;
    }

    /**
     * 查询findQuantity个距离queryPoint最近的点
     *
     * @param queryPoint
     * @param findQuantity
     * @return
     */
    public List<Point> getRangePoints(Point queryPoint, double findQuantity) {

        this.queryPoint.set(queryPoint);
        this.findQuantity.set(12);
        while(true){
            pointQueue.set(new PriorityQueue<>(new PointComparator()));
            //log.info("{} " , pointQueue.size() );
            this.findNearest(0, true);
            double dis = util.distance(queryPoint ,pointQueue.get().peek() ) ;
            if(dis > findQuantity * 2 + 50.0 ){
                break;
            }

            this.findQuantity.set(this.findQuantity.get() * 2)  ;
            //log.info("{} {} " , dis , this.findQuantity );
        }

        List<Point> newPoints = new ArrayList<>();
        while (!pointQueue.get().isEmpty()) {

            double dis = util.distance(queryPoint, pointQueue.get().peek());
            if(dis <= findQuantity){
                newPoints.add(pointQueue.get().peek());
            }
            //log.info("大顶堆记录  {} {}  {}  {}  ", dis,  String.format( "%.6f",calculateDistance(queryPoint, pointQueue.peek() ) ),queryPoint, pointQueue.peek());
            pointQueue.get().poll();
        }
        Collections.reverse(newPoints);
        return newPoints;
    }

    /**
     * 返回距离最近的点
     *
     * @param queryPoint
     * @return
     */
    public Point getNearestPoint(Point queryPoint) {
        List<Point> pointList = getNearestPoints(queryPoint, 30)  ;
        log.info("{}",this.queryPoint.get());
        double minDistance = Double.MAX_VALUE;
        Point resultPoint = pointList.get(0);
        for(Point point : pointList){
            double distance = util.distance(point ,queryPoint);
            //log.info("kdtree  {}" ,distance);
            if(distance < minDistance){
                minDistance = distance;
                resultPoint = point;
            }
        }
        //log.info("jieshu############ ");
        return  resultPoint ;
    }

    public static void main(String[] args) throws IOException {
        Logger log = LoggerFactory.getLogger(" test ");
        Util util = new Util();
        List<Point> points = util.readPoints("/Users/zheheng/Desktop/Git/GitLab/idea_code/route_plan_ project/transfer/src/main/java/com/yg84/matching/util/KDTree/KDTreeData.txt");

        log.info("{}  0points {}", points, points.get(0));

        Point point2 = new Point(113.549232, 22.216097);
        KDTree kdTree = new KDTree(points, new Point());

        ExecutorService executorService = Executors.newFixedThreadPool(200);
        for(int i = 0 ; i < 300 ;  ++i){
            Point point1 = new Point(113.551931, 22.216464);
            point1.setIndex(i);
            executorService.submit(()->{
                log.info(" {} " ,Util.distance( point1, kdTree.getNearestPoint(point1) ) );

            });
        }

        executorService.shutdown();
        //double dis2 = util.distance(point1, point2);
    }

}

