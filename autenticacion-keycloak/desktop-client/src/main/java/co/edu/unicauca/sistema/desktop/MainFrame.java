package co.edu.unicauca.sistema.desktop;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

/**
 * Main window: a login form plus one button per gateway endpoint.
 *
 * <p>All network calls run in a {@link SwingWorker}, so the Event Dispatch Thread is never blocked.
 * The UI shows the roles contained in the token for information only; whether a call is allowed
 * is decided by the server, and the 401/403 replies are shown to the user as such.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /** Endpoints offered by the UI: service prefix x role-specific suffix. */
    static final List<String> SERVICES = List.of("a", "b");
    static final List<String> ENDPOINTS = List.of("hello_student", "hello_coordinator", "hello_company", "hello_all");

    private final transient ClientConfig config;
    private final transient KeycloakClient keycloakClient;
    private final transient GatewayClient gatewayClient;

    private final JTextField usernameField = new JTextField(12);
    private final JPasswordField passwordField = new JPasswordField(12);
    private final JButton loginButton = new JButton("Log in");
    private final JButton logoutButton = new JButton("Log out");
    private final JLabel sessionLabel = new JLabel("Not authenticated");
    private final JTextArea output = new JTextArea(12, 60);
    private final JPanel endpointPanel = new JPanel(new GridLayout(0, 4, 6, 6));

    /** Access token of the current session; {@code null} while logged out. */
    private transient String token;

    public MainFrame(ClientConfig config) {
        super("Sistema - Keycloak desktop client");
        this.config = config;
        this.keycloakClient = new KeycloakClient(config);
        this.gatewayClient = new GatewayClient(config);
        buildUi();
        setEndpointButtonsEnabled(false);
    }

    private void buildUi() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        loginPanel.setBorder(BorderFactory.createTitledBorder("Keycloak login (realm sistema)"));
        loginPanel.add(new JLabel("User:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(logoutButton);
        logoutButton.setEnabled(false);
        loginButton.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        logoutButton.addActionListener(e -> doLogout());

        sessionLabel.setFont(sessionLabel.getFont().deriveFont(Font.BOLD));
        JPanel top = new JPanel(new BorderLayout());
        top.add(loginPanel, BorderLayout.NORTH);
        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sessionPanel.add(sessionLabel);
        top.add(sessionPanel, BorderLayout.SOUTH);

        endpointPanel.setBorder(BorderFactory.createTitledBorder("Endpoints (through the API Gateway)"));
        for (String service : SERVICES) {
            for (String endpoint : ENDPOINTS) {
                String path = "/" + service + "/" + endpoint;
                JButton button = new JButton("microservice-" + service + " / " + endpoint.replace("hello_", ""));
                button.setToolTipText("GET " + path);
                button.addActionListener(e -> callEndpoint(path));
                endpointPanel.add(button);
            }
        }

        output.setEditable(false);
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        output.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createTitledBorder("Result"));
        scroll.setPreferredSize(new Dimension(760, 220));

        root.add(top, BorderLayout.NORTH);
        root.add(endpointPanel, BorderLayout.CENTER);
        root.add(scroll, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        loginButton.setEnabled(false);
        append("Requesting token for '" + username + "' ...");

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return keycloakClient.login(username, new String(password));
            }

            @Override
            protected void done() {
                java.util.Arrays.fill(password, '\0');
                loginButton.setEnabled(true);
                try {
                    onLoginSuccess(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    onLoginFailure(e.getCause());
                }
            }
        }.execute();
    }

    private void onLoginSuccess(String accessToken) {
        token = accessToken;
        JwtClaims claims = JwtClaims.parse(accessToken);
        List<String> roles = claims.rolesOf(config.clientId());
        sessionLabel.setText("Authenticated as " + claims.username() + "  |  roles: " + roles);
        sessionLabel.setForeground(new Color(0, 110, 0));
        passwordField.setText("");
        logoutButton.setEnabled(true);
        setEndpointButtonsEnabled(true);
        append("Login OK. Token received (" + accessToken.length() + " chars). Roles: " + roles);
    }

    private void onLoginFailure(Throwable cause) {
        token = null;
        sessionLabel.setText("Not authenticated");
        sessionLabel.setForeground(Color.RED.darker());
        append("LOGIN ERROR: " + cause.getMessage());
    }

    private void doLogout() {
        token = null;
        sessionLabel.setText("Not authenticated");
        sessionLabel.setForeground(Color.BLACK);
        logoutButton.setEnabled(false);
        setEndpointButtonsEnabled(false);
        append("Logged out. The token was discarded.");
    }

    private void callEndpoint(String path) {
        String currentToken = token;
        append("GET " + path);
        new SwingWorker<ApiResponse, Void>() {
            @Override
            protected ApiResponse doInBackground() throws IOException {
                return gatewayClient.get(currentToken, path);
            }

            @Override
            protected void done() {
                try {
                    ApiResponse response = get();
                    append("  -> " + response.describe());
                    if (response.status() == 401) {
                        doLogout();   // expired/invalid token: force a new login
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    append("  -> NETWORK ERROR: " + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void setEndpointButtonsEnabled(boolean enabled) {
        for (java.awt.Component component : endpointPanel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    private void append(String line) {
        output.append(line + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }
}
