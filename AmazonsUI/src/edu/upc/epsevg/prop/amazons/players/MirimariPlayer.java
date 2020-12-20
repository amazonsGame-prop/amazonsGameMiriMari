package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Move;
import edu.upc.epsevg.prop.amazons.SearchType;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author mirimari
 */
public class MirimariPlayer implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private int nodes_explorats;
    private int max_prof;
    private int prof;
    private boolean timeout = false;

    public MirimariPlayer(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        this.timeout = true;
    }

    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        //Inicialitzem a 0 els comptadors
        nodes_explorats = 0;
        max_prof = 0;
        prof = 0;
        timeout = false;
        
      //  this.s = s;
       
        Point amazonFrom = null
                , amazonTo = null
                , arrowTo = null
                , arrow = null; 
        
        float aux;
        float heurist = Float.MIN_VALUE; // -inifinito
        float beta = Float.MAX_VALUE; // +infinito
       
        CellType color = s.getCurrentPlayer();

     //   while(!timeout){
            ArrayList<Point> amazonPositions = new ArrayList<>();
            for (int i=0; i < s.getNumberOfAmazonsForEachColor(); ++i) { // per a cada amazona mirem quin moviment és millor
                if(timeout) break;
                Point amazona = s.getAmazon(color, i);
               // System.out.println("ENTRA DIFERENT AMAZONA" + amazona);
                amazonPositions = s.getAmazonMoves(amazona, false); //preguntar pel BOOLEÀ

              //  System.out.println("Possibles moviments" + amazonPositions.size());

                //Recorrem el tauler per a guardar les posicions lliures
                ArrayList<Point> emptyPositions = new ArrayList<>();
                for (int m = 0; m < s.getSize(); ++m){
                    if(timeout) break;
                    for (int n = 0; n < s.getSize(); ++n){
                        if(s.getPos(m, n) == CellType.EMPTY){
                             Point buida = new Point(m, n);
                             emptyPositions.add(buida);
                        } 
                    }
                }
                //Afegim la posició inicial de l'amazona que quedarà lliure
                emptyPositions.add(amazona);


                for (int x = 0; x < amazonPositions.size(); ++x) {
                   if(timeout)   break;
                    // System.out.println("Posicio possible moviment" + amazonPositions.get(x));
                    Point possibleMoviment = amazonPositions.get(x);
                    // "s" és una còpia del tauler, per tant es pot manipular sense perill
                    GameStatus tauler = new GameStatus(s);          
                    tauler.moveAmazon(amazona, possibleMoviment); // mou la fitxa

                    //Treiem la posició del moviment de la llista de posicions lliures
                    ArrayList<Point> empty = new ArrayList<>(emptyPositions);
                    empty.remove(possibleMoviment);

                    //Recorrem totes les posicions lliures del tauler per escollir el millor moviment de la fletxa
                    for(int j = 0; j < empty.size(); ++j){
                       if(timeout)  break;
                        // System.out.println("Posicions lliures" + empty.size());
                        arrow = empty.get(j);
                        GameStatus tauler_aux = new GameStatus(tauler);
                        tauler_aux.placeArrow(arrow);
                        //Treiem la posició on movem la fletxa de la llista de posicions lliures
                        ArrayList<Point> emptyAux = new ArrayList<>(empty);
                        emptyAux.remove(arrow);
                       // System.out.println("Posicions lliures quitando sitio flecha" + emptyAux.size());
                        aux = min_v(tauler_aux, emptyAux, color.opposite(color), heurist, beta, 0);

                        if(max_prof < prof){
                            max_prof = prof;
                        }
                        if (aux >= heurist) {
                            //moviment = new Move(amazona, possibleMoviment, );
                            amazonFrom = amazona;
                            amazonTo = possibleMoviment;
                            arrowTo = arrow;
                            heurist = aux;
                        }
                    }
                }
            }
//        }

        return new Move(amazonFrom, amazonTo, arrowTo, nodes_explorats, max_prof, SearchType.MINIMAX);
    }

    public float max_v(GameStatus estat, ArrayList<Point> emptyPositions, CellType color, float alfa, float beta, int profunditat) {
        float valor = 0;
        
        if ( (estat.isGameOver()) || estat.getEmptyCellsCount() == 0 || !timeout ) {
           // System.out.println("GAMEOVER");
            if ( estat.isGameOver() && estat.GetWinner() == color ) valor = Float.MAX_VALUE;
            else if( estat.isGameOver() && estat.GetWinner() == color.opposite(color) ) valor = Float.MIN_VALUE;
            else{
              //  System.out.println("HEURISTICA");
              /*  MirimariHeuristica heuristica = new MirimariHeuristica(estat);
                valor = heuristica.calculaHeuristica(emptyPositions, color.opposite(color)); */
                valor = heuristica(estat, color.opposite(color));
            }

            ++nodes_explorats;
            prof = profunditat;
           
            return valor;
        }
        
        ArrayList<Point> amazonPositions = new ArrayList<>();
        for (int i=0; i < estat.getNumberOfAmazonsForEachColor(); ++i) { // per a cada fitxa mirem quin moviment és millor
            if(timeout) break;
            Point amazona = estat.getAmazon(color, i);
            //Si l'amazona no té més possibles moviments
            if (estat.getAmazonMoves(amazona, false).isEmpty()) {
                valor = Float.MIN_VALUE;      

                ++nodes_explorats;
                prof = profunditat;


               // return valor;
            }
            else {
                valor = Float.MIN_VALUE;
                amazonPositions = estat.getAmazonMoves(amazona, false); 

                ArrayList<Point> emptyPos = new ArrayList<>(emptyPositions);
                //Afegim la posició inicial de l'amazona que quedarà lliure
                emptyPos.add(amazona);

                for (int x = 0; x < amazonPositions.size(); ++x) {
                    if(timeout) break;
                    Point possibleMoviment = amazonPositions.get(x);
                    // "s" és una còpia del tauler, per tant es pot manipular sense perill
                    GameStatus tauler = new GameStatus(estat);          
                    tauler.moveAmazon(amazona, possibleMoviment); // mou la fitxa

                    //Treiem la posició del moviment de la llista de posicions lliures
                    ArrayList<Point> empty = new ArrayList<>(emptyPos);
                    empty.remove(possibleMoviment);

                    for(int j = 0; j < empty.size(); ++j){
                        if(timeout) break;
                        Point arrow = empty.get(j);
                        GameStatus tauler_aux = new GameStatus(tauler);
                        tauler_aux.placeArrow(arrow);
                        //Treiem la posició on movem la fletxa de la llista de posicions lliures
                        ArrayList<Point> emptyAux = new ArrayList<>(empty);
                        emptyAux.remove(arrow);
                       // System.out.println("EMPY AUX MAX" + emptyAux.size());
                        valor = Math.max(valor, min_v(tauler_aux, emptyAux, color.opposite(color), alfa, beta, profunditat+1));
                        alfa = Math.max(valor, alfa);
                        if(beta <= alfa)    return valor; //poda alfa-beta
                    }
                }
            }
        }
        return valor;
    }
  
    public float min_v(GameStatus estat, ArrayList<Point> emptyPositions, CellType color, float alfa, float beta, int profunditat) {
        float valor = 0; //me pedia inicializar la variable
       // System.out.println("ENTRA");
        if ( (estat.isGameOver()) || estat.getEmptyCellsCount() == 0 || !timeout ) {
          //  System.out.println("GAMEOVER");
            if ( estat.isGameOver() && estat.GetWinner() == color ) valor = Float.MIN_VALUE;
            else if( estat.isGameOver() && estat.GetWinner() == color.opposite(color) ) valor = Float.MAX_VALUE;
            else{
              //  System.out.println("HEURISTICA");
               /*  MirimariHeuristica heuristica = new MirimariHeuristica(estat);
                valor = heuristica.calculaHeuristica(emptyPositions, color.opposite(color)); */
                valor = heuristica(estat, color.opposite(color));
            }

            ++nodes_explorats;
            prof = profunditat;
            
           
            return valor;
        } 
       
        ArrayList<Point> amazonPositions = new ArrayList<>();
        for (int i=0; i < estat.getNumberOfAmazonsForEachColor(); ++i) { // per a cada fitxa mirem quin moviment és millor
            if(timeout)    break;
            Point amazona = estat.getAmazon(color, i);
           // System.out.println("ARRIBA 2" + amazona);
            //Si l'amazona no té més possibles moviments
            if (estat.getAmazonMoves(amazona, true).isEmpty()) {
               // System.out.println("sense moviments");
                valor = Float.MAX_VALUE;      

                ++nodes_explorats;
                prof = profunditat;


               // return valor;
            }
            else {
                valor = Float.MAX_VALUE;
                amazonPositions = estat.getAmazonMoves(amazona, true); 
                
                ArrayList<Point> emptyPos = new ArrayList<>(emptyPositions);
                //Afegim la posició inicial de l'amazona que quedarà lliure
                emptyPos.add(amazona);

                for (int x = 0; x < amazonPositions.size(); ++x) {
                    if(timeout) break;
                    Point possibleMoviment = amazonPositions.get(x);
                    // "s" és una còpia del tauler, per tant es pot manipular sense perill
                    GameStatus tauler = new GameStatus(estat);          
                    tauler.moveAmazon(amazona, possibleMoviment); // mou la fitxa

                    //Treiem la posició del moviment de la llista de posicions lliures
                    ArrayList<Point> empty = new ArrayList<>(emptyPos);
                    empty.remove(possibleMoviment);

                    for(int j = 0; j < empty.size(); ++j){
                        if(timeout) break;
                        Point arrow = empty.get(j);
                        GameStatus tauler_aux = new GameStatus(tauler);
                        tauler_aux.placeArrow(arrow);
                        //Treiem la posició on movem la fletxa de la llista de posicions lliures
                        ArrayList<Point> emptyAux = new ArrayList<>(empty);
                        emptyAux.remove(arrow);
                      //  System.out.println("EMPY AUX MIN" + emptyAux.size());
                        valor = Math.min(valor, max_v(tauler_aux, emptyAux, color.opposite(color), alfa, beta, profunditat+1));
                        beta = Math.min(valor, beta);
                        if(beta <= alfa)    return valor; //poda alfa-beta
                    }
                }
            } 
        }
        return valor;
    }
    
    //Heurística simple
    public float heuristica(GameStatus estat, CellType color){
        int jo = 0;
        int contrari = 0;
        
        int qn = estat.getNumberOfAmazonsForEachColor();
        
        for(int i = 0; i < qn; ++i){
            Point amazona = estat.getAmazon(color, i);
            jo += estat.getAmazonMoves(amazona, true).size();
        }
        for(int i = 0; i < qn; ++i){
            Point amazona = estat.getAmazon(color.opposite(color), i);
            int mov = estat.getAmazonMoves(amazona, false).size();
            contrari += mov;
            if(mov < 3) contrari -= 10000;
            if(mov == 0)    contrari -= 100000;
        }
        
        return jo - contrari; 
    }
}

