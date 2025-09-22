package problemset;

public class Problem3 {

	public static void main(String[] args) {
        String text = "To be or not to be, that is the question;"
                + " Whether 'tis nobler in the mind to suffer"
                + " the slings and arrows of outrageous fortune,"
                + " or to take arms against a sea of troubles,"
                + " and by opposing end them?";

        int spaces = 0;
        int vowels = 0;
        int letters = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);

            if (ch == ' ') spaces++;

            // simple letter check (A-Z or a-z)
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) {
                letters++;
                char c = (char) (ch | 32); // to lower quick (works for A-Z)
                if (c=='a' || c=='e' || c=='i' || c=='o' || c=='u') {
                    vowels++;
                }
            }
        }

        int consonants = letters - vowels;

        System.out.println("The text contained vowels: " + vowels);
        System.out.println("consonants: " + consonants);
        System.out.println("spaces: " + spaces);
    }

}
