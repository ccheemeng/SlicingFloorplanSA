class Room {
    protected final String id;
    protected final ImList<Pair<Integer, Integer>> corners;
    
    public Room(String id, int width, int height) {
        this.id = id;
        ImList<Pair<Integer, Integer>> corners = new ImList<Pair<Integer, Integer>>();
        corners = corners.add(new Pair<Integer, Integer>(width, height));
        if (width != height) {
            corners = corners.add(new Pair<Integer, Integer>(height, width));
        }
        this.corners = corners;
    }

    protected Room(String id, ImList<Pair<Integer, Integer>> corners) {
        this.id = id;
        this.corners = corners;
    }
    
    public static Room combineTwoRooms(Room r1, Room r2, String operator) {
        ImList<Pair<Integer, Integer>> newCorners = new ImList<Pair<Integer, Integer>>();
        for (Pair<Integer, Integer> corner1 : r1.corners) {
            for (Pair<Integer, Integer> corner2 : r2.corners) {
                if (operator == "V") {
                    Pair<Integer, Integer> newCorner = new Pair<Integer, Integer>(
                            corner1.first() + corner2.first(),
                            Math.max(corner1.second(), corner2.second()));
                    newCorners = Room.updateCorners(newCorners, newCorner);
                } else if (operator == "H") {
                    Pair<Integer, Integer> newCorner = new Pair<Integer, Integer>(
                            Math.max(corner1.first(), corner2.first()),
                            corner1.second() + corner2.second());
                    newCorners = Room.updateCorners(newCorners, newCorner);
                }
            }
        }
        return new Room("(" + r1.id + "," + r2.id + "," + operator + ")", newCorners);
    }

    private static ImList<Pair<Integer, Integer>> updateCorners(ImList<Pair<Integer, Integer>> corners, Pair<Integer, Integer> newCorner) {
        if (corners.isEmpty()) {
            return corners.add(newCorner);
        }
        ImList<Pair<Integer, Integer>> returnCorners = new ImList<Pair<Integer, Integer>>();
        for (int i = 0; i < corners.size(); i++) {
            Pair<Integer, Integer> corner = corners.get(i);
            if (Room.isRedundant(corner, newCorner)) {
                return corners;
            }
            if (!(Room.isRedundant(newCorner, corner))) {
                returnCorners = returnCorners.add(corner);
            }
        }
        return returnCorners.add(newCorner);
    }

    public static boolean isRedundant(Pair<Integer, Integer> existingCorner, Pair<Integer, Integer> newCorner) {
        return !(newCorner.first() < existingCorner.first() || newCorner.second() < existingCorner.second());
    }
    
    public String getId() {
        return this.id;
    }

    public ImList<Pair<Integer, Integer>> getCorners() {
        return this.corners;
    }
/**
    @Override
    public boolean equals(Object obj) {
        return this == obj ||
            (obj instanceof Room room && this.id == room.id);
    }
**/
    @Override
    public String toString() {
        return String.format("%s %s", this.id, this.corners.toString());
    }
}
