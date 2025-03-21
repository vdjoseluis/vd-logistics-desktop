package com.vdjoseluis.vdlogistics;

import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import com.vdjoseluis.vdlogistics.ui.LoginFrame;

/**
 *
 * @author José Luis Vásquez Drouet
 */
public class Main {

    public static void main(String[] args) {
        FirebaseConfig.initializeFirebase();
        new LoginFrame().setVisible(true);
    }
}
