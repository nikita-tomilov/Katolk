package com.programmer74.katolk.dao

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

@Entity
@Table(name = "USER_", uniqueConstraints = [UniqueConstraint(columnNames = ["USER_NAME"])])
data class User(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  var id: Long? = null,

  @Column(name = "USER_NAME")
  private var username: String? = null,

  @Column(name = "PASSWORD")
  private var password: String? = null,

  @Column(name = "ACCOUNT_EXPIRED")
  private var accountExpired: Boolean = false,

  @Column(name = "ACCOUNT_LOCKED")
  private var accountLocked: Boolean = false,

  @Column(name = "CREDENTIALS_EXPIRED")
  private var credentialsExpired: Boolean = false,

  @Column(name = "ENABLED")
  private var enabled: Boolean = false,

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "USERS_AUTHORITIES",
      joinColumns = [JoinColumn(name = "USER_ID", referencedColumnName = "ID")],
      inverseJoinColumns = [JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")])
  @OrderBy
  @JsonIgnore
  private var authorities: Collection<com.programmer74.katolk.dao.Authority>? = null
) : UserDetails {

  override fun getAuthorities(): Collection<com.programmer74.katolk.dao.Authority> {
    return authorities!!
  }

  override fun getUsername(): String {
    return username!!
  }

  override fun getPassword(): String {
    return password!!
  }

  override fun isEnabled(): Boolean {
    return enabled
  }

  override fun isAccountNonExpired(): Boolean {
    return !accountExpired
  }

  override fun isAccountNonLocked(): Boolean {
    return !accountLocked
  }

  override fun isCredentialsNonExpired(): Boolean {
    return !credentialsExpired
  }

  fun safeId(): Long = id ?: error("should-never-happen")
}