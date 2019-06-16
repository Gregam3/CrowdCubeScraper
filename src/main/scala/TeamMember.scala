import Gender.Gender

object Gender extends Enumeration {
  type Gender = Value
  val Male, Female, Other, Indeterminate = Value
}

class TeamMember(val name: String, val gender: Gender, val isCeo: Boolean)
