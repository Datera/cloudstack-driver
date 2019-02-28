===================================
DATERA CloudStack Driver Repository
===================================
.. list-table:: CloudStack Driver Version with Datera Product and Supported Hypervisor(s)
   :header-rows: 1
   :class: version-table

   * - CloudStack Release
     - Driver Version
     - Capabilities Introduced
     - Supported Datera Product Versions
     - Supported Hypervisors
   * - 4.11.2
     - v2.0.2
     - Dynamic Primary Storage
     - 3.2
     - KVM

  
=====================
Configuration Options
=====================
.. list-table:: Description of Datera CloudStack driver configuration options
   :header-rows: 1
   :class: config-ref-table

   * - Configuration option = Default value
     - Description
   * - ``MVIP`` = ``None``
     - (String) Datera API management vip.
   * - ``SVIP`` = ``None``
     - (String) Datera Access vip.
   * - ``clusterAdminUsername`` = ``None``
     - (String) Datera API user name.
   * - ``clusterAdminPassword`` = ``None``
     - (String) Datera API user password.
   * - ``numReplicas`` = ``2``
     - (Int) Number of replicas to create a volume.

===================
Configuration Steps
===================
1. Deploy CloudStack by following the instructions http://docs.cloudstack.apache.org/projects/cloudstack-installation/en/4.11/
2. Download Datera CloudStack driver from releases
3. Save it to 

   ``/usr/share/cloudstack-management/lib/``
4. Restart CloudStack management service

   ``service cloudstack-management restart``
