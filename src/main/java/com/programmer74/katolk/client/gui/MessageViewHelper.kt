package com.programmer74.katolk.client.gui

import com.google.common.collect.Lists
import com.programmer74.katolk.common.data.MessageJson
import com.programmer74.katolk.common.data.User

fun buildHTML(messages: List<MessageJson>, me: User): String {
  var from: String
  var msg: String

  val historySb = StringBuilder()

  historySb.append("<html>")
  historySb.append("<head>")
  historySb.append("   <script language=\"javascript\" type=\"text/javascript\">")
  historySb.append("       function toBottom(){")
  historySb.append("           window.scrollTo(0, document.body.scrollHeight);")
  historySb.append("       }")
  historySb.append("   </script>")
  historySb.append("<style>")
  historySb.append("body {\n" +
      "    background-color: white;\n" +
      "    color: white;\n" +
      "    font-family: Arial, Helvetica, sans-serif;\n" +
      "    font-size: 11pt;\n" +
      "}\n" +
      "div {\n" +
      "\tmax-width: 70%;\n" +
      "    clear:both;\n" +
      "    padding: 10px; \n" +
      "}\n" +
      ".my {\n" +
      "\tbackground-color: green;\n" +
      "    text-align: left;\n" +
      "    float:right;\n" +
      "    border-radius: 25px 25px 5px 25px;\n" +
      "}\n" +
      ".his {\n" +
      "\tbackground-color: darkgreen;\n" +
      "    text-align: left;\n" +
      "    float:left;\n" +
      "    border-radius: 25px 25px 25px 5px;\n" +
      "}")
  historySb.append("</style>")
  historySb.append("</head>")
  historySb.append("<body onload='toBottom()'>")

  var prevUsername = ""
  var shouldIncludeUsername: Boolean

  for (message in Lists.reverse(messages)) {

    from = message.author
    shouldIncludeUsername = false
    if (from != prevUsername) {
      prevUsername = from
      shouldIncludeUsername = true
    }
    msg = message.body

    historySb.append("<div class=\"" + (if (message.authorId == me.id) "my" else "his") + "\">")

    msg = msg.replace("[<]".toRegex(), "&lt;")
    msg = msg.replace("[>]".toRegex(), "&gt;")
    msg = msg.replace("\n|\r\n|\n\r|\r".toRegex(), "<br>")

    if (shouldIncludeUsername) {
      historySb.append("<b>").append(from).append("</b><br>")
    }
    historySb.append(msg)

    historySb.append("</div>\n")
  }
  historySb.append("</body>")
  historySb.append("</html>")
  return historySb.toString()
}