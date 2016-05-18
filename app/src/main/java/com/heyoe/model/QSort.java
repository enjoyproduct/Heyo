package com.heyoe.model;

import java.util.ArrayList;

/**
 * Created by dell17 on 5/18/2016.
 */
public class QSort {
    private ArrayList<Long> arrDate;
    public QSort (ArrayList<Long> arrayList) {
        this.arrDate = new ArrayList<>();
        arrDate.addAll(arrayList);
    }
    // This method sorts an array and internally calls quickSort
    public void sort(){
        int left = 0;
        int right = arrDate.size()-1;
        quickSort(left, right);
    }
    public ArrayList<Long> getResult() {
        return arrDate;
    }
    // It takes the left and the right end of the array as the two cursors.
    private void quickSort(int left,int right){
        // If both cursor scanned the complete array quicksort exits
        if(left >= right)
            return;
        // For the simplicity, we took the right most item of the array as a pivot
        long pivot = arrDate.get(right);
        int partition = partition(left, right, pivot);
        // Recursively, calls the quicksort with the different left and right parameters of the sub-array
        quickSort(0, partition-1);
        quickSort(partition+1, right);
    }
            // This method is used to partition the given array and returns the integer which points to the sorted pivot index
    private int partition(int left,int right, long pivot){
        int leftCursor = left - 1;
        int rightCursor = right;
        while(leftCursor < rightCursor){
            while(arrDate.get(++leftCursor) < pivot);
            while(rightCursor > 0 && arrDate.get(--rightCursor) > pivot);
            if(leftCursor >= rightCursor){
                break;
            }else{
                swap(leftCursor, rightCursor);
            }
        }
        swap(leftCursor, right);
        return leftCursor;
    }
            // This method is used to swap the values between the two given index
    public void swap(int left,int right){
        long tempLeft = arrDate.get(left);
        long tempRight = arrDate.get(right);
        arrDate.remove(left);
        arrDate.add(left, tempRight);
        arrDate.remove(right);
        arrDate.add(right, tempLeft);
    }

}
