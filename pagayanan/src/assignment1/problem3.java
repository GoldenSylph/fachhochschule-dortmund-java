public class problem3 {
    public static void main(String[] args) {
        String text = "To be or not to be, that is the question;"
                +"Whether `tis nobler in the mind to suffer"
                +" the slings and arrows of outrageous fortune,"
                +" or to take arms against a sea of troubles,"
                +" and by opposing end them?";

        int spaces = 0,
            vowels = 0,
            letters = 0;

        // YOUR CODE HERE
        
        // Code that computes the number of spaces, vowels, and consonants
        String lowerText = text.toLowerCase();

        for (int i = 0; i < text.length(); i++) {
            char ch = lowerText.charAt(i);

            if (Character.isLetter(ch)) {
                letters++;

                if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                    vowels++;
                }
            }
            else if (ch == ' ') {
                spaces++;
            }
        }

        System.out.println("The text contained vowels: " + vowels + "\n"
                + "consonants: " + (letters - vowels) + "\n"
                + "spaces: " + spaces);
    }
}
