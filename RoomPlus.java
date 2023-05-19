import java.util.Optional;

class RoomPlus extends Room{
    private final ImList<Pair<Integer, Integer>> rightChildPos;
    private final Optional<Pair<RoomPlus, RoomPlus>> children;
    private final ImList<Pair<Integer, Integer>> indices;

    public RoomPlus(Room room) {
        super(room.id, room.corners);
        this.rightChildPos = new ImList<Pair<Integer, Integer>>();
        this.children = Optional.<Pair<RoomPlus, RoomPlus>>empty();
        this.indices = new ImList<Pair<Integer, Integer>>();
    }

    private RoomPlus(String id,
            ImList<Pair<Integer, Integer>> rightChildPos,
            Optional<Pair<RoomPlus, RoomPlus>> children,
            ImList<Pair<Integer, Integer>> indices,
            ImList<Pair<Integer, Integer>> corners) {
        super(id, corners);
        this.rightChildPos = rightChildPos;
        this.children = children;
        this.indices = indices;
    }

    public static RoomPlus combineTwoRoomPlus(RoomPlus r1, RoomPlus r2, String operator) {
        ImList<Pair<Integer, Integer>> newRightChildPos = new ImList<Pair<Integer,Integer>>();
        ImList<Pair<Integer, Integer>> newIndices = new ImList<Pair<Integer, Integer>>();
        ImList<Pair<Integer, Integer>> newCorners = new ImList<Pair<Integer, Integer>>();
        Pair<Integer, Integer> newCorner = new Pair<Integer, Integer>(0, 0);
        Pair<Integer, Integer> newPos = new Pair<Integer, Integer>(0, 0);
        ImList<Integer> updatedIndices = new ImList<Integer>();
        for (int i = 0; i < r1.corners.size(); i++) {
            Pair<Integer, Integer> corner1 = r1.corners.get(i);
            for (int j = 0; j < r2.corners.size(); j++) {
                Pair<Integer, Integer> corner2 = r2.corners.get(j);
                if (operator == "V") {
                    newCorner = new Pair<Integer, Integer>(
                            corner1.first() + corner2.first(),
                            Math.max(corner1.second(), corner2.second()));
                    updatedIndices = checkCorners(newCorners, newCorner);
                    newPos = new Pair<Integer, Integer>(corner1.first(), 0);
                } else if (operator == "H") {
                    newCorner = new Pair<Integer, Integer>(
                            Math.max(corner1.first(), corner2.first()),
                            corner1.second() + corner2.second());
                    updatedIndices = checkCorners(newCorners, newCorner);
                    newPos = new Pair<Integer, Integer>(0, corner1.second());
                }
                Pair<Integer, Integer> newIndex = new Pair<Integer, Integer>(i, j);
                newRightChildPos = updateList(newRightChildPos, updatedIndices, newPos);
                newIndices = updateList(newIndices, updatedIndices, newIndex);
                newCorners = updateList(newCorners, updatedIndices, newCorner);
            }
        }
        RoomPlus newRoom = new RoomPlus("(" + r1.id + "," + r2.id + "," + operator + ")",
                newRightChildPos, Optional.<Pair<RoomPlus, RoomPlus>>of(
                    new Pair<RoomPlus, RoomPlus>(r1, r2)),
                newIndices, newCorners);
        return newRoom;
    }

    private static ImList<Integer> checkCorners(ImList<Pair<Integer, Integer>> corners,
            Pair<Integer, Integer> newCorner) {
        ImList<Integer> newIndices = new ImList<Integer>();
        for (int i = 0; i < corners.size(); i++) {
            Pair<Integer, Integer> corner = corners.get(i);
            if (!(Room.isRedundant(newCorner, corner))) {
                newIndices = newIndices.add(i);
            }
        }
        return newIndices.add(-1);
    }

    private static <T> ImList<T> updateList(ImList<T> list, ImList<Integer> indices, T t) {
        ImList<T> newList = new ImList<T>();
        for (int i : indices) {
            if (i < 0) {
                newList = newList.add(t);
            } else {
                newList = newList.add(list.get(i));
            }
        }
        return newList;
    }

    public String getDims(int index) {
        return getDims(index, new Pair<Integer, Integer>(0, 0));
    }

    private String getDims(int index, Pair<Integer, Integer> origin) {
        if (this.children.isEmpty()) {
            Pair<Integer, Integer> dim = super.corners.get(index);
            return String.format("%d,%d %d %d\n", origin.first(), origin.second(),
                    dim.first(), dim.second());
        } else {
            Pair<Integer, Integer> offset = this.rightChildPos.get(index);
            return this.children.get().first().getDims(this.indices.get(index).first(), origin) +
                this.children.get().second().getDims(this.indices.get(index).second(),
                        new Pair<Integer, Integer>(offset.first() + origin.first(),
                            offset.second() + origin.second()));
        }
    }

    public ImList<Pair<Integer, Integer>> getCorners() {
        return super.corners;
    }
}
