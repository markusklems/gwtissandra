package edu.kit.aifb.cass.client;

import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.kit.aifb.cass.shared.CassandraParam;
import edu.kit.aifb.cass.shared.FieldVerifier;
import edu.kit.aifb.cass.shared.model.Followee;
import edu.kit.aifb.cass.shared.model.Follower;
import edu.kit.aifb.cass.shared.model.Tweet;
import edu.kit.aifb.cass.shared.model.User;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Gwtissandra implements EntryPoint {
	
	private static User me = new User("Anonymous Bosch");

	private User getUser() {
		return me;
	}

	private void setUser(String username) {
		Gwtissandra.me = new User(username);
	}

	// Top page elements
	final MenuBar menu = getMenu();
	// Center page elements
	final TabPanel leftPanel = getTabPanel();
	final TextBox tweetText = new TextBox();
	final Button sendButton = new Button("Send Tweet");
	final VerticalPanel myTeets = new VerticalPanel();
	final VerticalPanel timelineTweets = new VerticalPanel();
	final VerticalPanel publicUserlineTweets = new VerticalPanel();
	private TabPanel tabPanel = getTabPanel();
	// Bottom page elements
	final Label status = new Label("");
	final Label errorLabel = new Label();

	/**
	 * Create a remote service proxy to talk to the server-side services.
	 */
	private static final TweetServiceAsync tweetService = GWT
			.create(TweetService.class);
	private static final FollowerServiceAsync followerService = GWT
			.create(FollowerService.class);

	/**
	 * Update userline or timeline.
	 * 
	 */
	class UpdateLineHandler implements SelectionHandler<Integer> {

		@Override
		public void onSelection(final SelectionEvent<Integer> event) {
			// Clear right-side view.
			RootPanel.get("right").clear();
			// Three Tabs can be selected:
			if (event.getSelectedItem().equals(0)) {
				// Retrieve the 30 most recent own tweets from Cassandra.
				tweetService.getUserline(getUser(),
						new Long(System.currentTimeMillis()), 30,
						new AsyncCallback<Tweet[]>() {
							public void onFailure(Throwable caught) {
								caught.printStackTrace();
							}

							public void onSuccess(Tweet[] result) {
								updateFeedElement(myTeets, result);
							}
						});
				// View followee's recent tweets.
			} else if (event.getSelectedItem().equals(1)) {
				tweetService.getTimeline(getUser(),
						new Long(System.currentTimeMillis()), 30,
						new AsyncCallback<Tweet[]>() {
							public void onFailure(Throwable caught) {
								caught.printStackTrace();
							}

							public void onSuccess(Tweet[] result) {
								updateFeedElement(timelineTweets, result);
							}
						});
				// View the 30 newest tweets of all tweets.
			} else {
				tweetService.getUserline(new User(
						CassandraParam.PUBLIC_USERLINE_KEY.getValue()),
						new Long(System.currentTimeMillis()), 30,
						new AsyncCallback<Tweet[]>() {
							public void onFailure(Throwable caught) {
								caught.printStackTrace();
							}

							public void onSuccess(Tweet[] result) {
								updateFeedElement(publicUserlineTweets, result);
							}
						});
			}
		}

		// helper method
		private void updateFeedElement(VerticalPanel feed, Tweet[] tweets) {
			feed.clear();
			for (Tweet t : tweets) {
				if (t != null) {
					// Add tweets the feed
					HorizontalPanel hp = new HorizontalPanel();
					hp.add(new UsernameLink(new User(t.getUsername())));
					hp.add(new Label(": " + t.getBody()));
					feed.add(hp);
				}
			}
		}

		// helper class
		class UsernameLink extends Label {
			public UsernameLink(User user) {
				super(user.getName());
				// Make it look like a hyperlink.
				super.setStyleName("hyperlinkStyle");
				// Open a new view on the right side when someone clicks.
				super.addClickHandler(new UserlineClickHandler(user));
			}
		}

		// helper class
		class UserlineClickHandler implements ClickHandler {

			private User user;

			public UserlineClickHandler(User user) {
				this.user = user;
			}

			@Override
			public void onClick(ClickEvent event) {
				// Open the panel on the right side.
				RootPanel.get("right").clear();
				final DecoratorPanel dp = new DecoratorPanel();
				final VerticalPanel vp = new VerticalPanel();
				final VerticalPanel mainVp = new VerticalPanel();
				final VerticalPanel vpFollow = new VerticalPanel();
				vp.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
				vpFollow.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				dp.add(mainVp);
				mainVp.add(vpFollow);
				mainVp.add(vp);
				RootPanel.get("right").add(dp);
				// Add a Follow/Unfollow button.
				if (!user.getName().equals(getUser().getName())) {
					followerService.getFolloweesOf(getUser().getName(),
							new AsyncCallback<List<Followee>>() {

								@Override
								public void onFailure(Throwable caught) {
									caught.printStackTrace();
								}

								@Override
								public void onSuccess(List<Followee> followees) {
									System.out.println("Followees:");
									for (Followee f : followees) {
										System.out.println(f.getUsername());
									}
									if (followees.contains(getUser().getName())) {
										// Add Unfollow button
									} else {
										Button followButton = new Button(
												"FOLLOW");
										vpFollow.add(followButton);
										followButton
												.addClickHandler(new FollowHandler(
														getUser().getName(),
														user.getName()));
									}
								}

							});
				}

				// Add tweets of user.
				// Retrieve userline from Cassandra.
				tweetService.getUserline(user,
						new Long(System.currentTimeMillis()), 30,
						new AsyncCallback<Tweet[]>() {
							public void onFailure(Throwable caught) {
								caught.printStackTrace();
							}

							public void onSuccess(Tweet[] result) {
								updateFeedElement(vp, result);
							}

							private void updateFeedElement(VerticalPanel feed,
									Tweet[] tweets) {
								feed.clear();
								for (Tweet t : tweets) {
									if (t != null) {
										// Add tweets the feed
										HorizontalPanel hp = new HorizontalPanel();
										hp.add(new UsernameLink(new User(t
												.getUsername())));
										hp.add(new Label(": " + t.getBody()));
										feed.add(hp);
									}
								}
							}

						});
			}

		}
	}

	/**
	 * Handler for sending a tweet.
	 * 
	 */
	class SendTweetHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			sendTweet();
		}

		/**
		 * Send the tweet to the server and wait for a response.
		 */
		private void sendTweet() {
			// First, we validate the input.
			errorLabel.setText("");
			String tweetBody = tweetText.getText();
			if (!FieldVerifier.isValidTweet(tweetBody)) {
				errorLabel
						.setText("Tweets cannot be longer than 140 characters.");
				return;
			}

			// Then, we send the tweet to the server.
			String username = getUser().getName();
			Tweet tweet = new Tweet(username, tweetBody);
			tweetService.saveTweet(tweet, getUser().getFollowers(),
					new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}

						@Override
						public void onSuccess(Void result) {
							System.out.println("User " + getUser().getName()
									+ " sent the tweet.");
						}
					});
		}

	}

	/**
	 * Follow a user.
	 * 
	 */
	class FollowHandler implements ClickHandler {

		private String username;
		private String followee;

		public FollowHandler(String username, String followee) {
			this.username = username;
			this.followee = followee;
		}

		@Override
		public void onClick(ClickEvent event) {
			followerService.addFollowee(username, followee,
					new AsyncCallback<Void>() {

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}

						@Override
						public void onSuccess(Void result) {
							System.out.println("User " + username
									+ " is now following " + followee);
						}

					});
		}

	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		HorizontalPanel newTweet = new HorizontalPanel();
		newTweet.add(tweetText);
		newTweet.add(sendButton);
		getHome().add(newTweet);

		getHome().add(myTeets);
		getTimeline().add(timelineTweets);
		getPublicUserline().add(publicUserlineTweets);

		// Focus the cursor on the name field when the app loads
		tweetText.setFocus(true);
		tweetText.selectAll();

		// Top of the Web page
		RootPanel.get("top").add(menu);

		// Left side of the Web page
		RootPanel.get("left").add(leftPanel);

		// Right side of the Web page
		// Open userline of other users when clicked on their name/link.

		// Additional info
		RootPanel.get("error").add(errorLabel);
		RootPanel.get("status").add(status);

		// Add Event Handler
		sendButton.addClickHandler(new SendTweetHandler());
		getTabPanel().addSelectionHandler(new UpdateLineHandler());

	}

	/**
	 * 
	 * Log into the Twissandra service. Login is enough. Subsequent login
	 * attempts with different usernames will simply switch to different users.
	 * Therefore, no logout is needed.
	 * 
	 * @param username
	 *            User who logs in.
	 */
	public void login(final String username) {
		// Update the username
		setUser(username);
		// Retrieve the current user's followers.
		followerService.getFollowersOf(username,
				new AsyncCallback<List<Follower>>() {
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(List<Follower> followers) {
						me.setFollowers(followers);
					}
				});
		// Retrieve the current user's followees.
		followerService.getFolloweesOf(username,
				new AsyncCallback<List<Followee>>() {
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(List<Followee> followees) {
						me.setFollowees(followees);
					}
				});
	}

	// ************
	// GUI elements
	// ************
	public MenuBar getMenu() {

		// Create the dialog box
		final DialogBox dialogBox = createDialogBox();
		dialogBox.setGlassEnabled(true);
		dialogBox.setAnimationEnabled(true);

		// Create commands that will execute on menu item selection.
		Command loginCommand = new Command() {
			public void execute() {
				// Show the dialog box.
				dialogBox.center();
				dialogBox.show();
			}
		};

		// Create a menu bar
		MenuBar menu = new MenuBar();
		MenuItem item = new MenuItem("Switch user", loginCommand);
		menu.addItem(item);

		return menu;
	}

	/**
	 * helper method
	 * 
	 * @return A DialogBox for user login.
	 */
	private DialogBox createDialogBox() {
		// Create a dialog box and set the caption text
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setTitle("Log in");

		// Create a table to layout the content
		final VerticalPanel dialogContents = new VerticalPanel();
		final HorizontalPanel userNameContents = new HorizontalPanel();
		dialogContents.setSpacing(10);
		dialogBox.setWidget(dialogContents);

		// Set up the user name label and text box.
		userNameContents.add(new Label("Username "));
		final TextBox usernameTextBox = new TextBox();
		userNameContents.add(usernameTextBox);

		// Add a close button at the bottom of the dialog
		Button loginButton = new Button("Log in", new ClickHandler() {
			public void onClick(ClickEvent event) {
				// log in
				login(usernameTextBox.getText());
				dialogBox.hide();
			}
		});
		dialogContents.add(userNameContents);
		dialogContents.add(loginButton);
		dialogContents.setCellHorizontalAlignment(loginButton,
				HasHorizontalAlignment.ALIGN_RIGHT);
		return dialogBox;
	}

	@SuppressWarnings("deprecation")
	public TabPanel getTabPanel() {
		if (tabPanel == null) {
			// Create a tab panel
			tabPanel = new DecoratedTabPanel();
			tabPanel.setAnimationEnabled(true);

			// Add tab titles
			String[] tabTitles = new String[] { "My Tweets", "Following",
					"All Tweets" };
			VerticalPanel home = new VerticalPanel();
			VerticalPanel timeline = new VerticalPanel();
			VerticalPanel publicUserline = new VerticalPanel();
			tabPanel.add(home, tabTitles[0]);
			tabPanel.add(timeline, tabTitles[1]);
			tabPanel.add(publicUserline, tabTitles[2]);
			tabPanel.selectTab(0);
		}

		return tabPanel;
	}

	public VerticalPanel getHome() {
		if (tabPanel == null) {
			getMenu();
		}
		return (VerticalPanel) tabPanel.getWidget(0);
	}

	public VerticalPanel getTimeline() {
		if (tabPanel == null) {
			getMenu();
		}
		return (VerticalPanel) tabPanel.getWidget(1);
	}

	public VerticalPanel getPublicUserline() {
		if (tabPanel == null) {
			getMenu();
		}
		return (VerticalPanel) tabPanel.getWidget(2);
	}

}
