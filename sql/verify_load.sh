
#!/bin/bash

ar_cmpny=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_cmpny_ao')) from ar_cmpny_view;"`
ar_cstatr=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_cstatr_ao')) from ar_cstatr_view;"`         
ar_cstmr=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_cstmr_ao')) from  ar_cstmr_view;"`         
ar_cstsup=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_cstsup_ao')) from  ar_cstsup_view;"`        
ar_cstuniq=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_cstuniq_ao')) from  ar_cstuniq_view;"`       
ar_cycle=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_cycle_ao')) from  ar_cycle_view;"`         
ar_dataara=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_dataara_ao')) from  ar_dataara_view;"`       
ar_libac=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_libac_ao')) from  ar_libac_view;"`         
ar_owner=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_owner_ao')) from  ar_owner_view;"`         
ar_pgxrf=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_pgxrf_ao')) from  ar_pgxrf_view;"`         
ar_rosvc=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_rosvc_ao')) from  ar_rosvc_view;"`         
ar_rosvcex=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_rosvcex_ao')) from  ar_rosvcex_view;"`       
ar_rosvd=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_rosvd_ao')) from ar_rosvd_view;"`          
ar_route=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_route_ao')) from  ar_route_view;"`         
ar_rtetyp=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_rtetyp_ao')) from  ar_rtetyp_view;"`        
ar_rtstd=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_rtstd_ao')) from  ar_rtstd_view;"`         
ar_rtxrf=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_rtxrf_ao')) from ar_rtxrf_view;"`          
ar_sanal=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_sanal_ao')) from  ar_sanal_view;"`         
ar_servc=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_servc_ao')) from  ar_servc_view;"`         
ar_slsxrf=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_slsxrf_ao')) from  ar_slsxrf_view;"`        
ar_slxtr=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_slxtr_ao')) from  ar_slxtr_view;"`         
ar_svcpnd=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_svcpnd_ao')) from  ar_svcpnd_view;"`        
ar_svcvcr=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_svcvcr_ao')) from  ar_svcvcr_view;"`        
ar_svdef=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('ar_svdef_ao')) from  ar_svdef_view;"`         
customer_hierarchy=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('customer_hierarchy_ao')) from customer_hierarchy_view;"`
infdv03p=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('infdv03p_ao')) from  infdv03p_view;"`         
sc_sites=`psql -Atc "select count(*),pg_size_pretty(pg_relation_size('sc_sites_ao')) from  sc_sites_view;"`         


echo " Table Name         |    Count|Size "
echo "--------------------+------------------------"
echo " ar_cmpny           |   $ar_cmpny"
echo " ar_cstatr          |   $ar_cstatr"
echo " ar_cstmr           |   $ar_cstmr"
echo " ar_cstsup          |   $ar_cstsup"
echo " ar_cstuniq         |   $ar_cstuniq"
echo " ar_cycle           |   $ar_cycle"
echo " ar_dataara         |   $ar_dataara"
echo " ar_libac           |   $ar_libac"
echo " ar_owner           |   $ar_owner"
echo " ar_pgxrf           |   $ar_pgxrf"
echo " ar_rosvc           |   $ar_rosvc"
echo " ar_rosvcex         |   $ar_rosvcex"
echo " ar_rosvd           |   $ar_rosvd"
echo " ar_route           |   $ar_route"
echo " ar_rtetyp          |   $ar_rtetyp"
echo " ar_rtstd           |   $ar_rtstd"
echo " ar_rtxrf           |   $ar_rtxrf"
echo " ar_sanal           |   $ar_sanal"
echo " ar_servc           |   $ar_servc"
echo " ar_slsxrf          |   $ar_slsxrf"
echo " ar_slxtr           |   $ar_slxtr"
echo " ar_svcpnd          |   $ar_svcpnd"
echo " ar_svcvcr          |   $ar_svcvcr"
echo " ar_svdef           |   $ar_svdef"
echo " customer_hierarchy |   $customer_hierarchy"
echo " infdv03p           |   $infdv03p"
echo " sc_sites           |   $sc_sites"
echo "--------------------+------------------------"

