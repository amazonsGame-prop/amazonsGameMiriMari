/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import edu.upc.epsevg.prop.amazons.GameStatus;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author miria
 */
public class MirimariHeuristica {
    GameStatus estat;
    private int casellesNostres;
    private int casellesContrari;
    
    public MirimariHeuristica(GameStatus estat){
        casellesNostres = 0;
        casellesContrari = 0;
        this.estat = estat;
    }
    
    public float calculaHeuristica(ArrayList<Point> emptyPositions, CellType color){
        for(int i = 0; i < emptyPositions.size(); ++i){
            amazonaPropera(emptyPositions.get(i));
        }
        if(casellesNostres < casellesContrari)  return -casellesContrari*3;
        else    return casellesNostres*3;
    }
    
    public void amazonaPropera(Point p){
        boolean trobat = false;
        
    }
}
