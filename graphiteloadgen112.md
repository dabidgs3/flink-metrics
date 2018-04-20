Graphite loadgen112
sudo apt-get install graphite-web graphite-carbon
in /etc/graphite modified local_settings.py

dguzman@desoxyribose:/etc/graphite$ sudo graphite-manage syncdb
/usr/lib/python2.7/dist-packages/graphite/settings.py:249: UserWarning: SECRET_KEY is set to an unsafe default. This should be set in local_settings.py for better security
  warn('SECRET_KEY is set to an unsafe default. This should be set in local_settings.py for better security')
Operations to perform:
  Synchronize unmigrated apps: account, cli, render, whitelist, metrics, url_shortener, dashboard, composer, events, browser
  Apply all migrations: admin, contenttypes, tagging, auth, sessions
Synchronizing apps without migrations:
  Creating tables...
    Creating table account_profile
    Creating table account_variable
    Creating table account_view
    Creating table account_window
    Creating table account_mygraph
    Creating table dashboard_dashboard
    Creating table events_event
    Creating table url_shortener_link
    Running deferred SQL...
  Installing custom SQL...
Running migrations:
  Rendering model states... DONE
  Applying contenttypes.0001_initial... OK
  Applying auth.0001_initial... OK
  Applying admin.0001_initial... OK
  Applying contenttypes.0002_remove_content_type_name... OK
  Applying auth.0002_alter_permission_name_max_length... OK
  Applying auth.0003_alter_user_email_max_length... OK
  Applying auth.0004_alter_user_username_opts... OK
  Applying auth.0005_alter_user_last_login_null... OK
  Applying auth.0006_require_contenttypes_0002... OK
  Applying sessions.0001_initial... OK
  Applying tagging.0001_initial... OK

You have installed Django's auth system, and don't have any superusers defined.
Would you like to create one now? (yes/no): yes
Username (leave blank to use 'root'): hduser
Email address: hduser@inet.tu-berlin.de
Password: 
Password (again): 
Superuser created successfully.




sudo nano /etc/default/graphite-carbon
This only has one parameter, which dictates whether the service will start on boot. Change the value to "true":

    CARBON_CACHE_ENABLED=true


ost of this file is already configured correctly for our purposes. However, we will make a small change.

Turn on log rotation by adjusting setting this directive to true:

ENABLE_LOGROTATION = True

########SKIP storage aggregation


When you are finished, you can start Carbon by typing:

sudo service carbon-cache start



#######################
Install the components by typing:

sudo apt-get install apache2 libapache2-mod-wsgi

#########################
When the installation is complete, we should disable the default virtual host file, since it conflicts with our new file:

sudo a2dissite 000-default

####DATABASE

cd /var/lib/graphite
chown -R _graphite _graphite graphite.db





