# Content-Type Converter

Burp Suite extension to convert request bodies between different content types: JSON, XML, URL-encoded, and multipart form data.

## Requirements

- Java 21+
- Burp Suite

## Usage

Right-click on a request in an editable message window (Repeater, Intruder, Proxy interceptor) and select one of the conversion options.

## Supported Conversions

| From | To |
|------|-----|
| URL-encoded | JSON, XML, Multipart |
| JSON | XML, URL-encoded, Multipart |
| XML | JSON, URL-encoded, Multipart |
| Multipart | JSON, XML, URL-encoded |

GET requests are automatically converted to POST when converting.

## Examples

### URL-Encoded to JSON

**Before:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/x-www-form-urlencoded

username=admin&password=secret
```

**After:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/json;charset=UTF-8

{"username":"admin","password":"secret"}
```

### JSON to XML

**Before:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/json

{"username":"admin","password":"secret"}
```

**After:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/xml;charset=UTF-8

<?xml version="1.0" encoding="UTF-8"?>
<root>
  <username>admin</username>
  <password>secret</password>
</root>
```

### JSON to Multipart Form

**Before:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/json

{"username":"admin","password":"secret"}
```

**After:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryabc123

------WebKitFormBoundaryabc123
Content-Disposition: form-data; name="username"

admin
------WebKitFormBoundaryabc123
Content-Disposition: form-data; name="password"

secret
------WebKitFormBoundaryabc123--
```

### XML to URL-Encoded

**Before:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8"?>
<root>
  <username>admin</username>
  <password>secret</password>
</root>
```

**After:**
```
POST /test HTTP/1.1
Host: www.example.com
Content-Type: application/x-www-form-urlencoded;charset=UTF-8

username=admin&password=secret
```

## Building

```bash
./gradlew build
```

The JAR will be created at `build/libs/content-type-converter.jar`.
