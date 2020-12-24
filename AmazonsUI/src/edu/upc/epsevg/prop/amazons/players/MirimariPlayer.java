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
 * @author mirimari
 */
public class MirimariPlayer implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private float heurist;
    private int nodes_explorats;
    private int max_prof; //límit profunditat (profunditat màxima)

    private boolean timeout;
    private boolean iterative; //booleà per saber si s'utilitza iterative deepening o no

    /**
     * Constructora per cerca limitada amb timeout
     * <p>Aquesta constructora es crida si es vol tenir en compte un temps límit sense profunditat limitada.</p>
     * @param name , string que representa el nom de l'objecte
     */
    public MirimariPlayer(String name) { // constructora per cerca limitada amb timeout
        this.name = name;
        this.iterative = true;
    }

    /**
     * Constructora per cerca limitada en profunditat
     * <p>Aquesta constructora es crida si es vol limitar la profunditat de la cerca sense tenir en compte un temps límit.</p>
     * @param name , string que representa el nom de l'objecte
     * @param profunditat , màxima profunditat amb la que volem limitar la cerca
     */
    public MirimariPlayer(String name, int profunditat) { // constructora per cerca limitada en profunditat
        this.name = name;
        this.max_prof = profunditat;
        this.iterative = false;
    }

    /**
     * Funció que retorna el nom.
     * 
     * @return name , nom del jugador
     */
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
        if (iterative) this.timeout = true;
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
        //Inicialitzem els comptadors
        nodes_explorats = 0;
        heurist = 0;
        timeout = false;
        this.s = s;

        Move m = null;
        Move aux = null;

        // Recorrem el tauler per a guardar les posicions lliures
        ArrayList<Point> emptyPositions_ini = new ArrayList<>();
        for (int i = 0; i < s.getSize(); ++i){
            for (int j = 0; j < s.getSize(); ++j){
                if (s.getPos(i, j) == CellType.EMPTY) {
                     Point buida = new Point(i, j);
                     emptyPositions_ini.add(buida);
                }
            }
        }

        if (iterative) {
            float max = Float.MIN_VALUE;
            for (int p = 1; p > 0; ++p) { //límit profunditat
                if (timeout) break;
                this.max_prof = p;
                aux = moviment(emptyPositions_ini);     
                
                if (max == Float.MIN_VALUE) max = heurist;
                if (heurist >= max) {
                    if (aux != null) {
                        m = aux;
                        max = heurist;
                    }
                }
                if (heurist == Float.MAX_VALUE) break; //Cas que guanyem
            }           
        }
        else m = moviment(emptyPositions_ini);
        
        m.setNumerOfNodesExplored(nodes_explorats);
        m.setMaxDepthReached(max_prof);

        return m;
    }

    /**
     * Funció on s'inicialitza el MiniMax
     * <p>S'inicialitzen les simulacions i paràmetres necessàris per a obtenir el millor moviment del jugador actual.</p>
     * @param emptyPositions_ini , llista que conté totes les posicions buides del tauler actual
     * @return m , millor moviment calculat 
     */
    private Move moviment (ArrayList<Point> emptyPositions_ini) {
        
        Point amazonFrom = null,
              amazonTo = null,
              arrowTo = null,
              amazona = null,
              possibleMoviment = null,
              arrow = null;

        float aux;
        
        CellType color = s.getCurrentPlayer();
        heurist = Float.MIN_VALUE; // -inifinito

        ArrayList<Point> amazonPositions = new ArrayList<>();
        for (int i = 0; i < s.getNumberOfAmazonsForEachColor(); ++i) { // per a cada amazona mirem quin moviment és millor
            if (timeout) break;
            amazona = s.getAmazon(color, i);
            amazonPositions = s.getAmazonMoves(amazona, false); // guardem tots els possibles moviments de l'amazona

            ArrayList<Point> emptyPositions = new ArrayList<>(emptyPositions_ini);
            // Afegim la posició inicial de l'amazona que quedarà lliure
            emptyPositions.add(amazona);

            for (int x = 0; x < amazonPositions.size(); ++x) {
                if (timeout) break;
                possibleMoviment = amazonPositions.get(x);
                GameStatus tauler = new GameStatus(s);
                tauler.moveAmazon(amazona, possibleMoviment); // mou la fitxa
                
                //Afegim tots els possibles moviments de les amazones contraries a la llista
                ArrayList<Point> emptyContraries = new ArrayList<>();
                for (int c = 0; c < tauler.getNumberOfAmazonsForEachColor(); ++c) { 
                    if (timeout) break;
                    Point amazonaContraria = tauler.getAmazon(color.opposite(color), c);
                    emptyContraries.addAll(tauler.getAmazonMoves(amazonaContraria, true));
                }

                // Recorrem totes les posicions lliures del tauler per escollir el millor moviment de la fletxa
                for (int j = 0; j < emptyContraries.size(); ++j){
                    if (timeout) break;
                    arrow = emptyContraries.get(j);
                    GameStatus tauler_aux = new GameStatus(tauler);
                    tauler_aux.placeArrow(arrow);
                    //Traiem la posició on movem la fletxa de la llista de posicions lliures
                    ArrayList<Point> emptyAux = new ArrayList<>(emptyContraries);
                    emptyAux.remove(arrow);
                    
                    aux = min_v(tauler_aux, emptyAux, color.opposite(color), Float.MIN_VALUE, Float.MAX_VALUE, this.max_prof - 1); // crida a minimax
                    
                    if (heurist == Float.MIN_VALUE) heurist = aux;
                    if (aux >= heurist) {
                        amazonFrom = amazona;
                        amazonTo = possibleMoviment;
                        arrowTo = arrow;
                        heurist = aux;
                    }
                }
            }
        }
        
        Move m = new Move(amazonFrom, amazonTo, arrowTo, nodes_explorats, max_prof, SearchType.MINIMAX);
        return m;
    }

    /**
     * Funció minimitzadora
     * <p>Retorna el valor heurístic més petit</p>
     * @param estat , tauler simulat
     * @param emptyPositions , llista que conté les posicions buides del tauler
     * @param color , CellType que indica el jugador (sempre serà player2)
     * @param alfa , -infinit
     * @param beta , +infinit
     * @param profunditat , profunditat actualitzada (comença sent la màxima profunditat i es decrementa en cada crida) 
     * @return valor , resultat heurístic més petit
     */
    private float min_v(GameStatus estat, ArrayList<Point> emptyPositions, CellType color, float alfa, float beta, int profunditat) {
        float valor = Float.MAX_VALUE;
        if ( estat.isGameOver() ||  timeout || profunditat == 0) {
            valor = heuristica(estat, color.opposite(color));
            ++nodes_explorats;
            return valor;
        }
        
        ArrayList<Point> amazonPositions = new ArrayList<>();
        for (int i = 0; i < estat.getNumberOfAmazonsForEachColor(); ++i) { // per a cada fitxa mirem quin moviment és millor
            if (timeout) break;
            Point amazona = estat.getAmazon(color, i);
            
            // Si l'amazona té possibles moviments
            if (!estat.getAmazonMoves(amazona, true).isEmpty()) {
                amazonPositions = estat.getAmazonMoves(amazona, false);

                ArrayList<Point> emptyPos = new ArrayList<>(emptyPositions);
                //Afegim la posició inicial de l'amazona que quedarà lliure
                emptyPos.add(amazona);

                for (int x = 0; x < amazonPositions.size(); ++x) {
                    if (timeout) break;
                    Point possibleMoviment = amazonPositions.get(x);
                    GameStatus tauler = new GameStatus(estat);
                    tauler.moveAmazon(amazona, possibleMoviment); // mou la fitxa
                    
                    //Afegim tots els possibles moviments de les amazones contraries a la llista
                    ArrayList<Point> emptyContraries = new ArrayList<>();
                    for (int c = 0; c < tauler.getNumberOfAmazonsForEachColor(); ++c) { 
                        if (timeout) break;
                        Point amazonaContraria = tauler.getAmazon(color.opposite(color), c);
                        emptyContraries.addAll(tauler.getAmazonMoves(amazonaContraria, true));
                    }
                    emptyContraries.remove(possibleMoviment);

                    for (int j = 0; j < emptyContraries.size(); ++j){
                        if (timeout) break;
                        Point arrow = emptyContraries.get(j);
                        GameStatus tauler_aux = new GameStatus(tauler);
                        tauler_aux.placeArrow(arrow);
                        //Traiem la posició on movem la fletxa de la llista de posicions lliures
                        ArrayList<Point> emptyAux = new ArrayList<>(emptyContraries);
                        emptyAux.remove(arrow);

                        valor = Math.min(valor, max_v(tauler_aux, emptyAux, color.opposite(color), alfa, beta, (profunditat-1)) );
                        beta = Math.min(valor, beta);
                        if (beta <= alfa) return valor; //poda alfa-beta
                    }
                }
            }
        }
        return valor;
    }
    
    /**
     * Funció maximitzadora
     * <p>Retorna el valor heurístic més gran</p>
     * @param estat , tauler simulat
     * @param emptyPositions , llista que conté les posicions buides del tauler
     * @param color , CellType que indica el jugador (sempre serà player1)
     * @param alfa , -infinit
     * @param beta , +infinit
     * @param profunditat , profunditat actualitzada (comença sent la màxima profunditat i es decrementa en cada crida) 
     * @return valor , resultat heurístic més gran
     */
    private float max_v(GameStatus estat, ArrayList<Point> emptyPositions, CellType color, float alfa, float beta, int profunditat) {
        float valor = Float.MIN_VALUE;
        if ( estat.isGameOver() ||  timeout || profunditat == 0) {
            valor = heuristica(estat, color);
            ++nodes_explorats;
            return valor;
        }
        
        ArrayList<Point> amazonPositions = new ArrayList<>();
        for (int i = 0; i < estat.getNumberOfAmazonsForEachColor(); ++i) { // per a cada fitxa mirem quin moviment és millor
            if (timeout) break;
            Point amazona = estat.getAmazon(color, i);
            
            // Si l'amazona té possibles moviments
            if (!estat.getAmazonMoves(amazona, true).isEmpty()) {
                amazonPositions = estat.getAmazonMoves(amazona, false);

                ArrayList<Point> emptyPos = new ArrayList<>(emptyPositions);
                //Afegim la posició inicial de l'amazona que quedarà lliure
                emptyPos.add(amazona);

                for (int x = 0; x < amazonPositions.size(); ++x) {
                    if (timeout) break;
                    Point possibleMoviment = amazonPositions.get(x);
                    GameStatus tauler = new GameStatus(estat);
                    tauler.moveAmazon(amazona, possibleMoviment); // mou la fitxa
                    
                    //Afegim tots els possibles moviments de les amazones contraries a la llista
                    ArrayList<Point> emptyContraries = new ArrayList<>();
                    for (int c = 0; c < tauler.getNumberOfAmazonsForEachColor(); ++c) { 
                        if (timeout) break;
                        Point amazonaContraria = tauler.getAmazon(color.opposite(color), c);
                        emptyContraries.addAll(tauler.getAmazonMoves(amazonaContraria, true));
                    }
                    emptyContraries.remove(possibleMoviment);

                    for (int j = 0; j < emptyContraries.size(); ++j){
                        if (timeout) break;
                        Point arrow = emptyContraries.get(j);
                        GameStatus tauler_aux = new GameStatus(tauler);
                        tauler_aux.placeArrow(arrow);
                        //Traiem la posició on movem la fletxa de la llista de posicions lliures
                        ArrayList<Point> emptyAux = new ArrayList<>(emptyContraries);
                        emptyAux.remove(arrow);

                        valor = Math.max(valor, min_v(tauler_aux, emptyAux, color.opposite(color), alfa, beta, (profunditat-1)) );
                        alfa = Math.max(valor, beta);
                        if (beta <= alfa) return valor; //poda alfa-beta
                    }
                }
            }
        }
        return valor;
    }

    /**
     * Funció d'avaluació
     * <p>Retorna un valor que reflexa l'estat del tauler a favor del player 1</p>
     * @param estat , tauler final de les simulacions
     * @param color , color igual a player 1
     * @return float ,  valor que reflexa l'estat del tauler a favor del player 1
     */
    private float heuristica(GameStatus estat, CellType color){
        int propia = 0;
        int contraria = 0;
        
        if (estat.isGameOver()){
            if (estat.GetWinner() == color) return Float.MAX_VALUE;
            else return Float.MIN_VALUE;
        }
        
        for (int i = 0; i < estat.getNumberOfAmazonsForEachColor(); ++i){
            Point amazona_propia = estat.getAmazon(color, i);
            Point amazona_contraria = estat.getAmazon(color.opposite(color), i);
             
            int mov_p = estat.getAmazonMoves(amazona_propia, true).size();
            int mov_c = estat.getAmazonMoves(amazona_contraria, true).size();
            
            propia -= Math.pow(10, (8 - mov_p));
            contraria -= Math.pow(10, (8 - mov_c));
            
            if (estat.getEmptyCellsCount() > 25) {
                propia += estat.getAmazonMoves(amazona_propia, false).size();
                contraria += estat.getAmazonMoves(amazona_contraria, false).size();
            }
        }
        return (propia - contraria);
    }
}