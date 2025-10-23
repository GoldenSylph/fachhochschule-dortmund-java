public class problem2 {
    public static void main(String[] args) {
        int nValues = 50;

        // (a) Using labeled continue instead of break
        outerLoop:
        for(int i = 2; i <= nValues; i++) {
            // (c) Only testing divisors up to square root of the number
            int limit = (int)Math.sqrt(i);

            for (int j = 2; j <= limit; j++) {
                if (i % j == 0) {
                    // (a) Labeled continue jumps to next iteration of outer loop
                    // (b) No isPrime variable needed - we use control flow instead
                    continue outerLoop;
                }
            }
            // If we reach here, no divisors were found, so i is prime
            System.out.println(i);
        }
    }
}
