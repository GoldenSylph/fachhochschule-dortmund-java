public class problem4 {
    public static void main(String[] args) {
        // Set up a String variable with the soliloquy from the previous question
        String text = "To be or not to be, that is the question;"
                +"Whether `tis nobler in the mind to suffer"
                +" the slings and arrows of outrageous fortune,"
                +" or to take arms against a sea of troubles,"
                +" and by opposing end them?";

        // Extract words from the text
        // Word definition: A word is a sequence of letters with punctuation removed
        String cleanedText = text.replaceAll("[^a-zA-Z\\s]", " ");
        String[] words = cleanedText.toLowerCase().split("\\s+");

        // Remove empty strings
        int wordCount = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                wordCount++;
            }
        }

        String[] validWords = new String[wordCount];
        int index = 0;
        for (String word : words) {
            if (!word.isEmpty()) {
                validWords[index++] = word;
            }
        }

        // Bubble sort implementation to sort words into alphabetical order
        boolean swapped;
        do {
            swapped = false;
            // Compare successive elements (0 and 1, 1 and 2, 2 and 3, and so on)
            for (int i = 0; i < validWords.length - 1; i++) {
                // If first element is greater than second, interchange the two elements
                if (validWords[i].compareTo(validWords[i + 1]) > 0) {
                    String temp = validWords[i];
                    validWords[i] = validWords[i + 1];
                    validWords[i + 1] = temp;
                    swapped = true;
                }
            }
        } while (swapped); // Repeat until no interchanges are necessary

        System.out.println("Words in alphabetical order:");
        for (int i = 0; i < validWords.length; i++) {
            System.out.printf("%3d. %s%n", i + 1, validWords[i]);
        }
    }
}
