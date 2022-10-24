package cc.invictusgames.site.service;

import cc.invictusgames.site.model.account.ForumAccountModel;
import cc.invictusgames.site.model.ProfileModel;
import cc.invictusgames.site.request.RequestHandler;
import cc.invictusgames.site.request.RequestResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class AccountDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RequestResponse response = RequestHandler.get("forum/account/login/%s", username);

        if (!response.wasSuccessful())
            throw new UsernameNotFoundException(username);

        ForumAccountModel account = new ForumAccountModel(response.asObject());
        if (account.getPassword() == null)
            throw new UsernameNotFoundException(username);

        response = RequestHandler.get("profile/%s?webResolved=true&includePermissions=true", account.getUuid().toString());
        if (!response.wasSuccessful())
            throw new UsernameNotFoundException(username);

        ProfileModel profile = new ProfileModel(response.asObject());
        return new User(profile.getUuid().toString(), account.getPassword(), this.getAuthority(profile));
    }

    private List<GrantedAuthority> getAuthority(ProfileModel account) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        account.getPermissions().forEach(s ->
                authorities.add(new SimpleGrantedAuthority(s)));

        return authorities;
    }

}
