import React, { Component } from 'react'
import { Timeline, TimelineEvent } from 'react-event-timeline'
import styled from 'styled-components'
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import UserLink from '../../components/UserLink';
import { BadgeEvent, PointEvent, MilestoneEvent, ChallengeEvent } from "./BadgeEvent";

const Wrapper = styled.div`
  width: 100%;
  justify-content: center;
  font-weight: 500;
`

export default class ProgressPage extends Component {
  render() {
    const { events = [1,2,3,4,5,6] } = this.props;

    return (
      <Wrapper>
        <div>
          {
            events.length > 0 &&
            <Timeline lineColor="hsl(203, 50%, 80%)">
              {
                events.map(e => {
                  switch (e % 4) {
                    case 0:
                      return <BadgeEvent user="Isuru" uid={23} badgeName="Superhero" ts={new Date(2018, 7, 15)} />;
                    case 1:
                      return <PointEvent user="Saman" uid={34} points={(Math.random()*500).toFixed(0)} pointName="submission" />
                    case 2:
                      return <MilestoneEvent user="Geetha" uid={45} level={(Math.random()*10).toFixed(0)} />
                    case 3:
                      return <ChallengeEvent user="Sukitha" uid={56} challengeName="Fix the bug #12345" />
                    default:
                      break;
                  }
                })
              }
            </Timeline>
          }
        </div>
      </Wrapper>
    )
  }
}
