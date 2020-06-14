package com.programmer74.katolk.dao

import org.springframework.security.core.GrantedAuthority
import javax.persistence.*

@Entity
@Table(name = "AUTHORITY", uniqueConstraints = [UniqueConstraint(columnNames = ["NAME"])])
data class Authority(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  var id: Long? = null,

  @Column(name = "NAME")
  var name: String? = null
) : GrantedAuthority {

  override fun getAuthority(): String {
    return name!!
  }
}