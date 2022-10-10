# KPITracker

A simple service, tracking some open-source startup related metrics and exporting them via prometheus API.

Currently tracked:
* Github: forks, stars, traffic, issues, dowloads
* Docker Hub: number of pulls

## Setting up

kpitracker is controlled using env variables:
* `GITHUB_PROJECT`: for example `metarank` 
* `GITHUB_REPO`: repo to watch for, like `metarank`
* `GITHUB_TOKEN`: your github access token
* `DOCKERHUB_PROJECT`: user name on DH.
* `DOCKERHUB_REPO`: repo name on DH.

## Example response

```
# HELP metarank_github_release_downloads number of total downloads
# TYPE metarank_github_release_downloads gauge
metarank_github_release_downloads 127.0
# HELP metarank_github_open_issues number of open issues
# TYPE metarank_github_open_issues gauge
metarank_github_open_issues 76.0
# HELP metarank_github_views_total number of views
# TYPE metarank_github_views_total gauge
metarank_github_views_total 762.0
# HELP metarank_github_visitors_today number of visitors today
# TYPE metarank_github_visitors_today gauge
metarank_github_visitors_today 8.0
# HELP metarank_github_traffic_referers_visitors visitors by referer
# TYPE metarank_github_traffic_referers_visitors gauge
metarank_github_traffic_referers_visitors{source="Google",} 70.0
metarank_github_traffic_referers_visitors{source="t.co",} 4.0
metarank_github_traffic_referers_visitors{source="medium.com",} 3.0
metarank_github_traffic_referers_visitors{source="github.com",} 43.0
metarank_github_traffic_referers_visitors{source="docs.metarank.ai",} 9.0
metarank_github_traffic_referers_visitors{source="ecosia.org",} 2.0
metarank_github_traffic_referers_visitors{source="blog.metarank.ai",} 36.0
metarank_github_traffic_referers_visitors{source="metarank.ai",} 17.0
metarank_github_traffic_referers_visitors{source="burakkarakan.com",} 1.0
metarank_github_traffic_referers_visitors{source="news.ycombinator.com",} 4.0
# HELP metarank_github_views_today number of views today
# TYPE metarank_github_views_today gauge
metarank_github_views_today 13.0
# HELP metarank_github_visitors_total number of visitors
# TYPE metarank_github_visitors_total gauge
metarank_github_visitors_total 275.0
# HELP metarank_dockerhub_pulls number of pulls
# TYPE metarank_dockerhub_pulls gauge
metarank_dockerhub_pulls 190.0
# HELP metarank_github_stars number of stars
# TYPE metarank_github_stars gauge
metarank_github_stars 1529.0
# HELP metarank_github_traffic_referers_views views by referer
# TYPE metarank_github_traffic_referers_views gauge
metarank_github_traffic_referers_views{source="Google",} 165.0
metarank_github_traffic_referers_views{source="t.co",} 4.0
metarank_github_traffic_referers_views{source="medium.com",} 3.0
metarank_github_traffic_referers_views{source="github.com",} 90.0
metarank_github_traffic_referers_views{source="docs.metarank.ai",} 20.0
metarank_github_traffic_referers_views{source="ecosia.org",} 2.0
metarank_github_traffic_referers_views{source="blog.metarank.ai",} 74.0
metarank_github_traffic_referers_views{source="metarank.ai",} 65.0
metarank_github_traffic_referers_views{source="burakkarakan.com",} 9.0
metarank_github_traffic_referers_views{source="news.ycombinator.com",} 4.0
# HELP metarank_github_forks number of forks
# TYPE metarank_github_forks gauge
metarank_github_forks 56.0

```
## License

This project is released under the Apache 2.0 license, as specified in the [License](LICENSE) file.