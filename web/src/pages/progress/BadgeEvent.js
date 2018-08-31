import React, { Component } from 'react'
import { Timeline, TimelineEvent } from 'react-event-timeline'
import styled from 'styled-components'
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import UserLink from '../../components/UserLink';
import TimeAgo from "react-timeago";

export class BadgeEvent extends Component {
  render() {
    const { user, uid, badgeName, badgeId, ts } = this.props;

    return (
      <TimelineEvent title={
        <span><UserLink user={user} uid={uid} /> 
        {" "} recieved a {"  "} <FontAwesomeIcon icon="award" /> {" "}
        <b>{badgeName}</b> badge</span>
      } 
        iconColor="hsl(0, 100%, 75%)"
        subtitle={<TimeAgo date={ts} />}
        icon={<FontAwesomeIcon icon="award" />} />
    )
  }
}

export class PointEvent extends Component {
  render() {
    const { user, uid, points, pointName, ts } = this.props;

    return (
      <TimelineEvent title={
        <span><UserLink user={user} uid={uid} /> 
        {" "} scored <FontAwesomeIcon icon="coins" /> {` ${points} from `} 
        <b>{pointName}</b></span>
      } 
        iconColor="hsl(51, 90%, 50%)"
        subtitle="2018-07-12 08:56 PM"
        icon={<FontAwesomeIcon icon="coins" />} />
    )
  }
}

export class MilestoneEvent extends Component {
  render() {
    const { user, uid, level, milestoneName, milestoneId, ts } = this.props;

    return (
      <TimelineEvent title={
        <span><UserLink user={user} uid={uid} /> 
        {" "} completed <FontAwesomeIcon icon="map-marker-alt" /> <b>{` Level ${level} `} </b>
        in milestone <b>{milestoneName}</b></span>
      } 
        iconColor="hsl(156, 69%, 67%)"
        subtitle="2018-07-12 08:56 PM"
        bubbleStyle={{ backgroundColor: 'hsl(156, 79%, 87%)' }}
        icon={<FontAwesomeIcon icon="map-marker-alt" />} />
    )
  }
}

export class ChallengeEvent extends Component {
  render() {
    const { user, uid, challengeId, challengeName, ts } = this.props;

    return (
      <TimelineEvent title={
        <span><UserLink user={user} uid={uid} /> 
        {" "} won the challenge <FontAwesomeIcon icon="trophy" /> <b>{` ${challengeName} `} </b>
        </span>
      } 
        iconColor="hsl(22, 80%, 63%)"
        subtitle="2018-07-12 08:56 PM"
        icon={<FontAwesomeIcon icon="trophy" />} />
    )
  }
}
