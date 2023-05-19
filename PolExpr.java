import java.util.Random;

class PolExpr {
    private static final int LIMIT = 100;

    public static ImList<String> moveOne(ImList<String> polExpr) {
        ImList<String> newPolExpr = polExpr;
        int lastOperandIndex = -1;
        for (int i = polExpr.size() - 1; i >= 0; i--) {
            if (polExpr.get(i) != "V" && polExpr.get(i) != "H") {
                lastOperandIndex = i;
                break;
            }
        }
        int i = 0;
        Random r = new Random();
        while (i < LIMIT) {
            int j = r.nextInt(lastOperandIndex);
            String operand1 = polExpr.get(j);
            if (operand1 != "V" && operand1 != "H") {
                for (int k = j + 1; k <= lastOperandIndex; k++) {
                    String operand2 = polExpr.get(k);
                    if (operand2 != "V" && operand2 != "H") {
                        newPolExpr = polExpr.set(j, operand2).set(k, operand1);
                        break;
                    }
                }
            }
            i++;
        }
        return newPolExpr;
    }

    public static ImList<String> moveTwo(ImList<String> polExpr) {
        ImList<String> newPolExpr = polExpr;
        int i = 0;
        Random r = new Random();
        while (i < LIMIT) {
            int j = r.nextInt(polExpr.size());
            String operator = polExpr.get(j);
            if (operator == "V" || operator == "H") {
                for (int k = j - 1; k >= 0; k--) {
                    String startOperator = polExpr.get(k);
                    if (startOperator != "V" && startOperator != "H") {
                        for (int l = k + 1; l < polExpr.size(); l++) {
                            String currOperator = polExpr.get(l);
                            if (currOperator == "V") {
                                newPolExpr = newPolExpr.set(l, "H");
                            } else if (currOperator == "H") {
                                newPolExpr = newPolExpr.set(l, "V");
                            } else {
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
            }
            i++;
        }
        return newPolExpr;
    }

    public static ImList<String> moveThree(ImList<String> polExpr, ImList<Integer> indices) {
        if (indices.isEmpty()) {
            System.out.println("Warning! M3 attempted on invalid Polish expression. Returning original expression.");
            return polExpr;
        }
        int i = new Random().nextInt(indices.size());
        int index = indices.get(i);
        String e1 = polExpr.get(index);
        String e2 = polExpr.get(index + 1);
        return polExpr.set(index, e2).set(index + 1, e1);
    }

    public static ImList<Integer> moveThreeIndices(ImList<String> polExpr) {
        ImList<Integer> indices = new ImList<Integer>();
        int numOfOperators = 0;
        if (!polExpr.isEmpty() && polExpr.get(0) == "H" || polExpr.get(0) == "V") {
            numOfOperators++;
        }
        for (int i = 0; i < polExpr.size() - 1; i++) {
            String e1 = polExpr.get(i);
            String e2 = polExpr.get(i + 1);
            if (e2 == "H" || e2 == "V") {
                numOfOperators++;
            }
            if (((e1 =="V" || e1 =="H") && (e2 != "V" && e2 != "H")) ||
                    ((e1 != "V" && e1 != "H") && (e2 == "V" || e2 == "H"))) {
                if (2 * numOfOperators < i + 1) {
                    indices = indices.add(i);
                }
                    }
        }
        return indices;
    }

    public static ImList<String> randomMove(ImList<String> polExpr) {
        ImList<String> newPolExpr = polExpr;
        Random r = new Random();
        int move = r.nextInt(3) + 1;
        if (move == 3) {
            ImList<Integer> moveThreeIndices = PolExpr.moveThreeIndices(polExpr);
            if (!moveThreeIndices.isEmpty()) {
                newPolExpr = PolExpr.moveThree(polExpr, moveThreeIndices);
            } else {
                move = r.nextInt(2) + 1;
            }
        }
        if (move == 1) {
            newPolExpr = PolExpr.moveOne(polExpr);
        } else if (move == 2) {
            newPolExpr = PolExpr.moveTwo(polExpr);
        }
        return newPolExpr;
    }
}
