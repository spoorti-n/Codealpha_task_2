package harry;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import opennlp.tools.util.Span;

public class Chatbotgui extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private StyledDocument doc;

    private ChatbotLogic chatbotLogic;

    public Chatbotgui() {
        super("NLP Chatbot");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 250));

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(250, 250, 255));
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 15));
        doc = chatArea.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(new Color(245, 245, 250));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 15));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 220), 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(220, 235, 250));
        sendButton.setFocusPainted(false);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        add(inputPanel, BorderLayout.SOUTH);

        try {
            chatbotLogic = new ChatbotLogic();
            appendMessage("Chatbot: Hello! I'm an NLP-powered chatbot. How can I help you today?", new Color(100, 150, 100));
        } catch (Exception e) {
            appendMessage("Chatbot: I'm sorry, I'm having trouble with my NLP models. Please check your project setup.", Color.RED);
            e.printStackTrace();
        }

        ActionListener sendAction = (ActionEvent e) -> {
            String userText = inputField.getText().trim();
            if (!userText.isEmpty()) {
                appendMessage("You: " + userText, new Color(50, 100, 200));
                String response = chatbotLogic.getChatbotResponse(userText);
                appendMessage("Chatbot: " + response, new Color(100, 150, 100));
                inputField.setText("");
            }
        };

        sendButton.addActionListener(sendAction);
        inputField.addActionListener(sendAction);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void appendMessage(String message, Color color) {
        try {
            Style style = chatArea.addStyle("ColorStyle", null);
            StyleConstants.setForeground(style, color);
            doc.insertString(doc.getLength(), message + "\n\n", style);
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Chatbotgui::new);
    }
}

class ChatbotLogic {
    private final Map<String, String> faqResponses;

    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private LemmatizerME lemmatizer;
    private NameFinderME nameFinderPerson;
    private NameFinderME nameFinderLocation;

    public ChatbotLogic() throws Exception {
        try (
            InputStream tokenModelIn = getClass().getResourceAsStream("/opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin");
            InputStream posModelIn = getClass().getResourceAsStream("/opennlp-en-ud-ewt-pos-1.3-2.5.4.bin");
            InputStream lemmatizerModelIn = getClass().getResourceAsStream("/opennlp-en-ud-ewt-lemmas-1.3-2.5.4.bin");
            InputStream personModelIn = getClass().getResourceAsStream("/en-ner-person.bin");
            InputStream locationModelIn = getClass().getResourceAsStream("/en-ner-location.bin")
        ) {
            if (tokenModelIn == null) {
                throw new FileNotFoundException("Model file not found in classpath: opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin");
            }
            if (posModelIn == null) {
                throw new FileNotFoundException("Model file not found in classpath: opennlp-en-ud-ewt-pos-1.3-2.5.4.bin");
            }
            if (lemmatizerModelIn == null) {
                throw new FileNotFoundException("Model file not found in classpath: opennlp-en-ud-ewt-lemmas-1.3-2.5.4.bin");
            }
            if (personModelIn == null) {
                throw new FileNotFoundException("Model file not found in classpath: en-ner-person.bin");
            }
            if (locationModelIn == null) {
                throw new FileNotFoundException("Model file not found in classpath: en-ner-location.bin");
            }

            tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
            posTagger = new POSTaggerME(new POSModel(posModelIn));
            lemmatizer = new LemmatizerME(new LemmatizerModel(lemmatizerModelIn));
            nameFinderPerson = new NameFinderME(new TokenNameFinderModel(personModelIn));
            nameFinderLocation = new NameFinderME(new TokenNameFinderModel(locationModelIn));
        }

        faqResponses = new HashMap<>();

        // FAQs...
        faqResponses.put("hi", "Hello! How can I assist you today?");
        faqResponses.put("hello", "Hello there! What's on your mind?");
        faqResponses.put("how many continent be there", "There are seven continents: Africa, Antarctica, Asia, Australia, Europe, North America, and South America.");
        faqResponses.put("what be the capital of france", "The capital of France is Paris.");
        faqResponses.put("who write the play hamlet", "William Shakespeare wrote the play Hamlet.");
        faqResponses.put("what be the large ocean in the world", "The largest ocean in the world is the Pacific Ocean.");
        faqResponses.put("what be the color of ruby", "The most common color of a ruby is red.");
        faqResponses.put("which planet be know as the red planet", "The planet known as the Red Planet is Mars.");
        faqResponses.put("who be the first man on the moon", "The first man to walk on the moon was Neil Armstrong.");
        faqResponses.put("what be the main gas in earth atmosphere", "The main gas in Earth's atmosphere is nitrogen.");
        faqResponses.put("who invent the light bulb", "Thomas Edison is credited with inventing the practical incandescent light bulb.");
        faqResponses.put("what be the small country in the world", "The smallest country in the world is Vatican City.");
        faqResponses.put("i have doubt regard my study", "i understand studying is difficult ,im more than grateful to help you with it");
        faqResponses.put("how be you", "I'm doing great, thank you for asking!");
        faqResponses.put("what be your purpose", "I am a conversational agent created to assist you.");
        faqResponses.put("your name", "I don't have a name, but you can call me Chatbot.");
        faqResponses.put("bye", "Goodbye! Have a nice day!");
    }

    public String getChatbotResponse(String input) {
        String[] tokens = tokenizer.tokenize(input);

        String[] posTags = posTagger.tag(tokens);
        String[] lemmas = lemmatizer.lemmatize(tokens, posTags);

        String cleanLemmatizedPhrase = Stream.of(lemmas)
                                            .map(s -> s.replaceAll("\\p{Punct}", ""))
                                            .collect(Collectors.joining(" "))
                                            .toLowerCase()
                                            .trim();

        System.out.println("DEBUG: Lemmatized Phrase = '" + cleanLemmatizedPhrase + "'");

        if (faqResponses.containsKey(cleanLemmatizedPhrase)) {
            return faqResponses.get(cleanLemmatizedPhrase);
        }

        if (tokens.length > 1) {
            String person = getNamedEntity(nameFinderPerson, input);
            String location = getNamedEntity(nameFinderLocation, input);

            if (person != null && location != null) {
                return "I recognize that " + person + " is a person and " + location + " is a location. That's interesting!";
            } else if (person != null) {
                return "I know that " + person + " is a person.";
            } else if (location != null) {
                return "I can see that " + location + " is a location.";
            }
        }

        return "I'm sorry, I don't understand that. Could you please rephrase your question?";
    }

    private String getNamedEntity(NameFinderME nameFinder, String originalInput) {
        String[] tokens = tokenizer.tokenize(originalInput);
        Span[] names = nameFinder.find(tokens);
        if (names.length > 0) {
            return names[0].getCoveredText(originalInput).toString();
        }
        return null;
    }
}
