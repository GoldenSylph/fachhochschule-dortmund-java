package problemset;

public class Problem4 {
	
	public static void main(String[] args) {
        String text = "To be or not to be, that is the question;"
                + " Whether 'tis nobler in the mind to suffer"
                + " the slings and arrows of outrageous fortune,"
                + " or to take arms against a sea of troubles,"
                + " and by opposing end them?";

        // words = sequences of letters
        String[] words = text.toLowerCase().split("[^a-zA-Z]+");

        // bubble sort (simple)
        for (int i = 0; i < words.length - 1; i++) {
            for (int j = 0; j < words.length - 1 - i; j++) {
                if (words[j].compareTo(words[j + 1]) > 0) {
                    String tmp = words[j];
                    words[j] = words[j + 1];
                    words[j + 1] = tmp;
                }
            }
        }

        for (String w : words) {
            if (!w.isEmpty()) System.out.println(w);
        }
    }

}
