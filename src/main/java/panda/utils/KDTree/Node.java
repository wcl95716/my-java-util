package panda.utils.KDTree;


import panda.utils.KDTree.Point;

public class Node {
    /**
     * KDTree 的node  树上节点的结构
     */
    public int ls = -1 ;
    public int rs = -1 ;
    public Point point = new Point(0,0);
    public Node(){

    }

    public String toString() {
        return String.format(" %d %d  %s ", this.ls, this.rs,this.point.toString()
        );
    }

}

