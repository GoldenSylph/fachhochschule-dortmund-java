package problemset;

public class Problem2 {

	public static void main(String[] args) {
        int nValues = 50;

        outer:
        for (int i = 2; i <= nValues; i++) {
            int lim = (int)Math.sqrt(i);
            for (int j = 2; j <= lim; j++) {
                if (i % j == 0) {
                    continue outer;
                }
            }
            System.out.println(i);
        }
    }

}
