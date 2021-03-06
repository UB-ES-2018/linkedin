package com.ub.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.ub.repository.UserRepository;
import com.ub.utils.LevenshteinDistance;

@Entity
@Table(name = "App_User", //
uniqueConstraints = { //
        @UniqueConstraint(name = "APP_USER_UK", columnNames = "User_Email") })
public class AppUser {
	
	@Id
	@GeneratedValue
    @Column(name = "User_Id", nullable = false)
	private long userId;
	
    @Column(name = "First_Name", length = 36, nullable = false)
	private String firstName;
    
    @Column(name = "Second_Name", length = 36, nullable = false)
	private String secondName;
    
    @Column(name = "Encryted_Password", length = 128, nullable = false)
	private String password;
    
    @Column(name = "User_Email", length = 64, nullable = false)
	private String email;
    
    @Column(name = "User_Country", length = 128, nullable = true)
    private String country;
    
    @Column(name = "Friends", nullable = true)
    @ElementCollection
    private List<String> friends = new ArrayList<String>();

    @Column(name = "Study_Index", nullable = true)
    private Integer sIndex = 0;

    @Column(name = "Job_Index", nullable = true)
	private Integer jIndex = 0;

	@ManyToMany(cascade = { 
    	    CascadeType.PERSIST, 
    	    CascadeType.MERGE
    	})
    @JoinTable(name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id" ,referencedColumnName = "Role_Id")
    		)
    private Set<Role> roles = new HashSet<>();
	
    @OneToMany(mappedBy = "user")
    private List<JobExperience> experiences = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<Studies> studies_list = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<Publication> publications_list = new ArrayList<>();
    
    @OneToMany(mappedBy = "user")
    private List<Comment> comments_list = new ArrayList<>();
    
    @OneToOne(mappedBy="user")
    private PhotoUser pic;

    @Column(name = "Postal_Code", nullable = true)
	private long postalCode;
    
	@Column(name = "Last_Search", nullable=true)
	private String userSearch;
    
    public AppUser() {
    }

	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getSecondName() {
		return secondName;
	}
	
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	
	public long getId() {
		return userId;
	}
	
	public void setId(long id) {
		this.userId = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}
	
	public void addRole(Role role) {
		roles.add(role);
        role.getUsers().add(this);
	}
	
	public void removeRole(Role role) {
		roles.remove(role);
		role.getUsers().remove(this);
    }
	
	public void addPublication(Publication p) {
		this.publications_list.add(p);
		p.setUser(this);
	}
	
	public void addStudies(Studies studies) {
		this.studies_list.add(studies);
		studies.setUser(this);
	}
	
	public void removeStudies(Studies studies) {
		this.studies_list.remove(studies);
	}
	
	public void addJobExperience(JobExperience job) {
		experiences.add(job);
		job.setUser(this);
	}
	
	public void removeJobExperience(JobExperience job) {
		experiences.remove(job);
    }
	
	public List<JobExperience> getExperiences() {
		return experiences;
	}

	public void setExperiences(List<JobExperience> experiences) {
		this.experiences = experiences;
	}

	public List<Studies> getStudies_list() {
		return studies_list;
	}

	public void setStudies_list(List<Studies> studies_list) {
		this.studies_list = studies_list;
	}

 	public long getPostalCode() {
		return postalCode;
	}
 	public void setPostalCode(long postalCode) {
		this.postalCode = postalCode;
	}
 	
 	public List<Publication> getPublication() {
 		return publications_list;
 	}
 	

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
    public List<String> getFriends() {
		return friends;
	}

	public void setFriends(List<String> friends) {

		this.friends = friends;
	}
	
	public void addFriend(String email) {

		if (!this.friends.contains(email) && !this.email.equals(email)) {
			this.friends.add(email);
		}
	}

	public List<Publication> getPublications_list() {
		return publications_list;
	}

	public void setPublications_list(List<Publication> publications_list) {
		this.publications_list = publications_list;
	}
	
	public List<Publication> sortedPublications(UserRepository repo) {
		
		List<Publication> tempP = new ArrayList<Publication>();
		List<Publication> sorted = new ArrayList<Publication>();
		
		this.friends.add(this.email);
		for (String friend: this.friends) {
			tempP = repo.findByEmail(friend).getPublications_list();
			for (Publication p: tempP) {
				sorted.add(p);
			}
		}
		Collections.sort(sorted);
		Collections.reverse(sorted);
		this.friends.remove(this.email);
		repo.save(this);
		return sorted;
		
	}
	
	public List<AppUser> getAppUserFriends(UserRepository repo) {
		List<AppUser> friends = new ArrayList<AppUser>();
		for (String friend: this.friends) {
			friends.add(repo.findByEmail(friend));
		}
		return friends;
	}
	
	public List<AppUser> makeSearch(UserRepository repo, String input, LevenshteinDistance lDist) {
		
		Map<Integer,List<AppUser>> scores = new HashMap<Integer,List<AppUser>>();
		List<AppUser> users = repo.findAll();
		boolean surName = false;
		if (input.contains(" ")) {
			surName = true;
		}
		input = input.toLowerCase().replace(" ","");
		
		

		Integer score;
		
		
		for (int i = 0; i < users.size(); i++) {
			if (!users.get(i).getEmail().equals(this.email)) {
				
				String toCompare = users.get(i).getFirstName();
				if (surName) {
					toCompare = users.get(i).getFirstName()+users.get(i).getSecondName();
				}

				toCompare = toCompare.toLowerCase().replace(" ","");
				System.out.println("inoput"+input+"To"+toCompare);

				List<AppUser> temp = new ArrayList<AppUser>();
				
				score = lDist.getDistance(input, toCompare);
	
				if (scores.containsKey(score)) {
					temp = scores.get(score);
				}
				temp.add(users.get(i));
				scores.put(score, temp);
			}
		}
		
		ArrayList<Integer> sortedKeys = new ArrayList<Integer>(scores.keySet()); 
	    Collections.sort(sortedKeys);

	    
	    List<AppUser> results = new ArrayList<AppUser>();
	    for (Integer i: sortedKeys) {
	    	if (i < 10) {
		    	for (AppUser u: scores.get(i)) {
		    		results.add(u);
		    	}
	    	}
	    	
	    }
	    return results;
	}
	
	
    
    public Integer getsIndex() {
		return sIndex;
	}

	public void setsIndex(Integer sIndex) {
		this.sIndex = sIndex;
	}

	public Integer getjIndex() {
		return jIndex;
	}

	public void setjIndex(Integer jIndex) {
		this.jIndex = jIndex;
	}

	public PhotoUser getPic() {
		return pic;
	}

	public void setPic(PhotoUser pic) {
		pic.setUser(this);
		this.pic = pic;
	}

	public String getUserSearch() {
		return userSearch;
	}

	public void setUserSearch(String userSearch) {
		this.userSearch = userSearch;
}

	
	
	
	
}