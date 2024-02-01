package org.example;

public class BinTree {

    private String data;
    private int weight;
    private BinTree left;
    private BinTree right;

    public BinTree(String data, int weight, BinTree left, BinTree right) {
        this.data = data;
        this.weight = weight;
        this.left = left;
        this.right = right;
    }

    public BinTree(String data, int weight) {
        this(data, weight, null, null);
    }

    public int getWeight() {
        return weight;
    }

    public String getData() {
        return data;
    }

    public BinTree getLeft() {
        return left;
    }

    public BinTree getRight() {
        return right;
    }
}
