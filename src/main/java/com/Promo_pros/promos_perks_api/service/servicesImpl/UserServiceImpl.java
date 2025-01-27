package com.Promo_pros.promos_perks_api.service.servicesImpl;

import com.Promo_pros.promos_perks_api.UserDto;
import com.Promo_pros.promos_perks_api.entity.User;
import com.Promo_pros.promos_perks_api.repository.UserRepository;
import com.Promo_pros.promos_perks_api.service.UserService;
import com.Promo_pros.promos_perks_api.util.BCryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

// was implementing UserService

@Service
public class UserServiceImpl implements UserDetailsService, UserService {

    //@Autowired
    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User save(UserDto userDto) {
        User newUser = new User();
        newUser.setEmail(userDto.getEmail());
        newUser.setName(userDto.getName());

        //BCryptUtil for password hashing
        String hashedPassword = BCryptUtil.generatedSecurePassword(userDto.getPassword());
        newUser.setPassword(hashedPassword);

        newUser.setMtn(userDto.getMtn());
        return userRepository.save(newUser);
    }

    //new
    @Override
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        //Use Optional to retrieve the user or throw an exception if not found
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        //Return UserDetails with roles and permissions
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Arrays.asList(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public User createUser(User user) {
        //Hash the password before saving the user
        String hashedPassword = BCryptUtil.generatedSecurePassword(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(new User());
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String loginUser(User user) {
//        or else might not be needed here
        User existingUser =  userRepository.findByEmail(user.getEmail()).orElse(null);
        if (existingUser != null && BCryptUtil.matchPasswords(user.getPassword(), existingUser.getPassword())) {
            return "You have logged in";
        }
        return "Invalid Login";

    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username); // Assuming you're using email as the username
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities() // Return user's roles/authorities
        );
    }
}
