package cc.invictusgames.site.model.account;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialLink {

    YOUTUBE("Youtube", "youtube-play", "https://youtube.com/"),
    TWITTER("Twitter", "twitter", "https://twitter.com/"),
    TWITCH("Twitch", "twitch", "https://twitch.tv/"),
    REDDIT("Reddit", "reddit", "https://reddit.com/u/"),
    GITHUB("GitHub", "github", "https://github.com/"),
    TELEGRAM("Telegram", "telegram", "https://telegram.me/");

    private final String displayName;
    private final String icon;
    private final String linkPrefix;

    public String getSettingName() {
        return "SOCIAL_" + this.name();
    }

}
