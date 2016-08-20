package com.heyoe_chat.model;

import java.util.ArrayList;

/**
 * Created by dell17 on 5/18/2016.
 */
public class QSort {
    private ArrayList<UserModel> arrUsers;
    public QSort (ArrayList<UserModel> arrayList) {
        this.arrUsers = new ArrayList<>();
        arrUsers.addAll(arrayList);
    }
    // This method sorts an array and internally calls quickSort
    public void sort(){
        int left = 0;
        int right = arrUsers.size()-1;
        quickSort(left, right);
    }
    public ArrayList<UserModel> getResult() {
        return arrUsers;
    }
    // It takes the left and the right end of the array as the two cursors.
    private void quickSort(int left,int right){
        // If both cursor scanned the complete array quicksort exits
        if(left >= right)
            return;
        // For the simplicity, we took the right most item of the array as a pivot
        long pivot = arrUsers.get(right).getLastMsgSentTime();
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
            while(arrUsers.get(++leftCursor).lastMsgSentTime > pivot);
            while(rightCursor > 0 && arrUsers.get(--rightCursor).lastMsgSentTime < pivot);
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
        UserModel tempLeft = arrUsers.get(left);
        UserModel tempRight = arrUsers.get(right);
        arrUsers.remove(left);
        arrUsers.add(left, tempRight);
        arrUsers.remove(right);
        arrUsers.add(right, tempLeft);
    }

}
