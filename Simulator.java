import java.time.Instant;
import java.time.Duration;
import java.util.Random;
import java.util.function.Function;

class Simulator {
    private final double p;
    private final double epsilon;
    private final double r;
    private final int k;
    private final int plotWidth;
    private final int plotHeight;
    private final double aspectRatioInfluence;
    public final ImList<Room> rooms;
    private static final int N = 10;
    private static final int NUM_MOVES_FOR_AVG = 20;
    private static final int LIMIT = 100;
    private static final double SUCCESS_RATE = 0.05;
    private static final int TIME_LIMIT = 600000;

    public Simulator(double p, double epsilon, double r, int k,
            int plotWidth, int plotHeight, double aspectRatioInfluence,
            ImList<Integer> widths, ImList<Integer> heights) {
        this.p = p;
        this.epsilon = epsilon;
        this.r = r;
        this.k = k;
        this.plotWidth = plotWidth;
        this.plotHeight = plotHeight;
        Function<Double, Double> scale = x -> ((N + 1) * x) / (N * x + 1);
        this.aspectRatioInfluence = scale.apply(aspectRatioInfluence);
        ImList<Room> rooms = new ImList<Room>();
        for (int i = 0; i < widths.size(); i++) {
            rooms = rooms.add(new Room(i + "", widths.get(i), heights.get(i)));
        }
        this.rooms = rooms;
    }
    
    public String simulate() {
        ImList<String> polExpr = startPolExpr(this.rooms);
        polExpr = anneal(polExpr);
        RoomPlus room = polExprToRoomPlus(polExpr);
        double cost = Double.POSITIVE_INFINITY;
        int i = 0;
        int index = i;
        for (Pair<Integer, Integer> dim : room.getCorners()) {
            double newCost = cost(dim.first(), dim.second());
            if (newCost < cost) {
                cost = newCost;
                index = i;
            }
            i++;
        }
        return room.getDims(index); 
    }

    public ImList<String> anneal(ImList<String> polExpr) {
        ImList<String> expr = polExpr;
        ImList<String> bestExpr = expr;
        ImList<String> newExpr = expr;
        double temp = calcStartTemp(polExpr);
        int moves = 0;
        int uphillMoves = 0;
        int rejectedMoves = 0;
        int moveLimit = this.k * this.rooms.size();
        double costExpr = calcCost(polExpr);
        double costBestExpr = costExpr;
        double costNewExpr = costExpr;
        double cost = 0;
        Random r = new Random();
        Instant start = Instant.now();
        long timeElapsed = 0;
        while (true) {
            moves = uphillMoves = rejectedMoves = 0;
            while (true) {
                newExpr = PolExpr.randomMove(expr);
                moves++;
                costNewExpr = calcCost(newExpr);
                cost = costNewExpr - costExpr;
                if (cost <= 0 || r.nextDouble() < Math.exp(-cost / temp)) {
                    if (cost > 0) {
                        uphillMoves++;
                    }
                    expr = newExpr;
                    costExpr = calcCost(expr);
                    if (costExpr < costBestExpr) {
                        bestExpr = expr;
                        costBestExpr = calcCost(bestExpr);
                    }
                } else {
                    rejectedMoves++;
                }
                if (uphillMoves > moveLimit || moves > 2 * moveLimit) {
                    break;
                }
            }
            System.out.printf("current temp: %f, %s (cost: %f)\n",
                    temp, bestExpr.toString(), costBestExpr);
            temp *= this.r;
            timeElapsed = Duration.between(start, Instant.now()).toMillis();
            if (rejectedMoves / moves >= 1 - SUCCESS_RATE || temp < this.epsilon || timeElapsed >= TIME_LIMIT) {
                break;
            }
        }
        return bestExpr;
    }

    public ImList<String> startPolExpr(ImList<Room> rooms) {
        ImList<String> polExpr = new ImList<String>();
        for (Room room : rooms) {
            polExpr = polExpr.add(room.getId()).add("V");
        }
        if (polExpr.size() >= 2) {
            polExpr = polExpr.remove(1);
        }
        return polExpr;
    }

    private double calcStartTemp(ImList<String> polExpr) {
        double cost = 0;
        int uphillMovesFound = 0;
        int i = 0;
        ImList<String> currPolExpr = polExpr;
        ImList<String> newPolExpr = polExpr;
        Random r = new Random();
        double currCost = calcCost(currPolExpr);
        double newCost = currCost;
        double deltaCost = 0;
        while (i < LIMIT && uphillMovesFound < NUM_MOVES_FOR_AVG) {
            newPolExpr = PolExpr.randomMove(currPolExpr);
            newCost = calcCost(newPolExpr);
            deltaCost = newCost - currCost;
            if (deltaCost > 0) {
                cost = (uphillMovesFound * cost + deltaCost) / (uphillMovesFound + 1);
                uphillMovesFound++;
            }
            currCost = newCost;
            i++;
        }
        return -cost / Math.log(this.p);
    }

    public double calcCost(ImList<String> polExpr) {
        return calcDimsAndCost(polExpr).second();
    }

    public Pair<Pair<Integer, Integer>, Double> calcDimsAndCost(ImList<String> polExpr) {
        Room room = polExprToRoom(polExpr);
        double cost = Double.POSITIVE_INFINITY;
        Pair<Integer, Integer> dims = new Pair<Integer, Integer>(0, 0);
        for (Pair<Integer, Integer> corner : room.getCorners()) {
            int x = corner.first();
            int y = corner.second();
            double costFunc = cost(x, y);
            if (costFunc < cost) {
                cost = costFunc;
                dims = corner;
            }
        }
        return new Pair<Pair<Integer, Integer>, Double>(dims, cost);
    }

    private double cost(int x, int y) {
        return this.aspectRatioInfluence * Math.abs((this.plotHeight / this.plotWidth) * x - y)
            + (1 - this.aspectRatioInfluence) * x * y;
    }
        
    public Room polExprToRoom(ImList<String> polExpr) {
        ImList<Room> newRooms = this.rooms;
        ImList<String> newPolExpr = polExpr;
        while (newPolExpr.size() > 1) {
            int v = newPolExpr.indexOf("V");
            int h = newPolExpr.indexOf("H");
            int i = h < 0 ? v : h;
            if (v * h >= 0) {
                i = Math.min(v, h);
            }
            String operator = newPolExpr.get(i);
            int r1 = indexOf(newRooms, newPolExpr.get(i - 2));
            int r2 = indexOf(newRooms, newPolExpr.get(i - 1));
            Room newRoom = Room.combineTwoRooms(newRooms.get(r1), newRooms.get(r2), operator);
            newRooms = newRooms.set(r1, newRoom).remove(r2);
            newPolExpr = newPolExpr.set(i, newRoom.getId()).remove(i - 2).remove(i - 2);
        }
        return newRooms.get(0);
    }

    public RoomPlus polExprToRoomPlus(ImList<String> polExpr) {
        ImList<RoomPlus> newRooms = new ImList<RoomPlus>();
        for (Room room : this.rooms) {
            newRooms = newRooms.add(new RoomPlus(room));
        }
        ImList<String> newPolExpr = polExpr;
        while (newPolExpr.size() > 1) {
            int v = newPolExpr.indexOf("V");
            int h = newPolExpr.indexOf("H");
            int i = h < 0 ? v : h;
            if (v * h >= 0) {
                i = Math.min(v, h);
            }
            String operator = newPolExpr.get(i);
            int r1 = indexOfPlus(newRooms, newPolExpr.get(i - 2));
            int r2 = indexOfPlus(newRooms, newPolExpr.get(i - 1));
            RoomPlus newRoom = RoomPlus.combineTwoRoomPlus(newRooms.get(r1), newRooms.get(r2), operator);
            newRooms = newRooms.set(r1, newRoom).remove(r2);
            newPolExpr = newPolExpr.set(i, newRoom.getId()).remove(i - 2).remove(i - 2);
        }
        return newRooms.get(0);
    }

    private int indexOf(ImList<Room> rooms, String id) {
        int i = 0;
        for (Room room : rooms) {
            if (id == room.getId()) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int indexOfPlus(ImList<RoomPlus> rooms, String id) {
        int i = 0;
        for (RoomPlus room : rooms) {
            if (id == room.getId()) {
                return i;
            }
            i++;
        }
        return -1;
    }
}
