
# DNS Block&Redirect Configurer


## Choose profile
Set environment variable `DNS` with DNS provider name (Cloudflare or NextDNS)

## Choose data sources
Each data source must be a link to a hosts file, e.g. https://raw.githubusercontent.com/Internet-Helper/GeoHideDNS/refs/heads/main/hosts/hosts

You can provide multiple sources split by coma:
https://first.com/hosts,https://second.com/hosts

### 1) Setup Redirects
Set sources to environment variable `REDIRECT`

Script will parce sources, filtering out redirects to 0.0.0.0

Thus, parcing lines

    0.0.0.0 domain.to.block
    1.2.3.4 domain.to.redirect

will keep only `1.2.3.4 domain.to.redirect` for the further redirect processing.


+ Redirect priority follows sources order. If domain appears more than one time, the first only IP will be applied.


### 2) Setup Blocklist
Set sources to environment variable `BLOCK`

Script will parce sources, keeping only redirects to 0.0.0.0 and domains without any ip.

Thus, parcing lines

    0.0.0.0 domain.to.block
    1.2.3.4 domain.to.redirect
    another.to.block

will keep only `domain.to.block` and `another.to.block` for the further block processing.

+ You may want to provide the same source for both `BLOCK` and `REDIRECT` for Cloudflare.

+ For NextDNS, the best option might be to set `REDIRECT` only, and then manually choose any blocklists at the Privacy tab.

## Setup Credentials

### Cloudflare
1) After signing up into a Cloudflare, navigate to Zero Trust tab and create an account.
- Free Plan has decent limits, so just choose it.
- Skip providing payment method step by choosing "Cancel and exit" (top right corner)
- Go back to Zero Trust tab

2) Create a Cloudflare API token, from https://dash.cloudflare.com/profile/api-tokens

with 2 permissions:

    Account.Zero Trust : Edit

    Account.Account Firewall Access Rules : Edit

Set API token to environment variable `AUTH_SECRET`

3) Get your Account ID from : https://dash.cloudflare.com/?to=/:account/workers

Set Account ID to environment variable `CLIENT_ID`

### NextDNS
1) Generate API KEY, from https://my.nextdns.io/account
   Set API KEY to environment variable `AUTH_SECRET`

2) Click on NextDNS logo. On the opened page, copy ID from Endpoints section.
   Set ID to environment variable `CLIENT_ID`

## Script Behaviour
### Cloudflare
Previously generated data will be removed. Script recognizes old data by two marks:

    1) Name prefix
        List: "Blocked websites by script" and "Override websites by script"
        Rule: "Rules set by script"
    2) Different Session id. Session id is stored in a description field.


After removing old data, new lists and rules will be generated and applied.

+ If you want to clear Cloudflare block/redirect settings, launch the script without providing sources in related environment variables. E.g. providing no value for environment variable `BLOCK` will cause removing old related data: lists and rules used to setup blocks.

### NextDNS

For `REDIRECT`:
+ Existing domain will be updated if redirect IP has changed
+ If new domains are provided, they will be added
+ The rest redirect settings are kept untouched

For `BLOCK`:
+ If new domains are provided, they will be added
+ The rest block settings are kept untouched

Previously generated data is removed ONLY when both `BLOCK` and `REDIRECT` sources were not provided.