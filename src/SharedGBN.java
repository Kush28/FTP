/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Johnny 28
 */
public class SharedGBN {
    public volatile int sf=0;
    public volatile int sn=0;
    public volatile int sw=Constants.WINDOW_SIZE-1;
    public volatile boolean canSend=true;
    
}
