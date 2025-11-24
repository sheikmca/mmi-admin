# ChangeLog

## 0.3.2 - 2023.02.03
**Enhancement**
- Update Dockerfile to support config overwriting

## 0.3.1 - 2023.01.11
**Enhancement**
- Support tenant detail and check password feign interface

## 0.3.0 - 2022.12.14

**Features**
- SB-37 mmi-admin support fixed api key
- update url format

------
## 0.2.2 - 2022.10-31

**Fix**
- PP-2193 [Notification]Change the role used by the user, should log out system automatically(update version uaa-server-starter to 0.2.3)
- PP-2214 [batch add user] When the ip start is greater than ip end, the return message is not correct
- PP-2215 [Security]Role management: search user list by full name, the search result is not correct(update version uaa-server-starter to 0.2.3)

**Enhancement**
- Upgrade mmi-admin-pojo to 0.1.2

------
## 0.2.1 - 2022.10-21

**Enhancement**

- Add websocket notification for user status change.
- Button level authority
- Add agency config support

------
## 0.2.0 - 2022.10-14
**Enhancement**
- Add menu api
- Import common-monitor for monitoring slow sql
- Upgrade UAA 0.2.0
- Refactor websocket
- Add a new feign interface for user

------
## 0.1.0 - 2022.09.19
**Features**
- User Management
    - add
    - edit
    - delete
    - enable/disable
    - searchList
    - batch add user
- Usergroup Management
    - add
    - edit
    - delete
    - search group tree
    - search user list
    - remove user
- Agency Management
    - add
    - edit
    - delete
    - enable/disable
    - searchList
- Role Management
    - add
    - edit
    - delete
    - enable/disable
    - search assigned user
    - user permission assign
    - searchList
- Recipient Group
    - add
    - edit
    - delete
    - searchList
    - assigned userList
      - notAssign userList
      - assignUser
      - removeUser
- password policy



